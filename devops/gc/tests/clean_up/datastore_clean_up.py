#  Copyright 2023 Google LLC
#  Copyright 2023 EPAM
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import argparse
import json
import re
from typing import Iterator, List

import dateutil

from dateutil import parser
from google.cloud import datastore

QUERY_LIMIT = 500

class DatastoreCleaner:

    FILTER_REGEXP = r"([\d\w.]+)(=|<=|>=|<|>)(.*)"

    def __init__(
        self,
        project: str,
        namespace: str = None,
        kind: str = None,
        query_filters: List[str] = None,
        query_limit: int = QUERY_LIMIT,
    ) -> None:
        self._client = datastore.Client(project=project)
        self._namespace = namespace
        self._kind = kind
        self._query_filters = self._prepare_filters(query_filters) if query_filters else query_filters
        self._query_limit = query_limit
        self._deleted_records_amount = 0

    def _prepare_filters(self, query_filters: List[str]) -> List[List[str]]:
        """Split raw string query filters into Lists of [<property_name>, <operator>, property_value].

        E.g., 'kind>="opendes:wks:autoTest_110641:1.1.0"' -> ['kind', '>=', 'opendes:wks:autoTest_110641:1.1.0']

        :param query_filters: List of query filters
        :type query_filters: List[str]
        :return: List of lists representing queries
        :rtype: List[List[str]]
        """
        filters = []
        for filter in query_filters:
            filter = re.match(self.FILTER_REGEXP, filter)
            property_name, operator, property_value = filter[1], filter[2], json.loads(filter[3])

            try:
                datetime = dateutil.parser.parse(property_value)
                property_value = datetime
            except Exception:
                pass

            filter = (property_name, operator, property_value)
            filters.append(filter)
        return filters

    def _query_pages(
        self,
        namespace: str,
        kind: str = "__kind__",
        filters: List[List[str]] = None
    ) -> Iterator[Iterator[datastore.Entity]]:
        """
        Yield a page of entities with using cursors.

        As [default] namespace is keyed as 1, it is not allowed to be passed in a query.
        Querying without namespace return values of [default] namespace.

        More info about [default] namespace is here: https://cloud.google.com/datastore/docs/concepts/metadataqueries#namespace_queries
        """
        if not isinstance(namespace, str):
            query = self._client.query(kind=kind)
        else:
            query = self._client.query(kind=kind, namespace=namespace)

        if filters:
            for filter in filters:
                query.add_filter(*filter)

        query.keys_only()
        query_iter = query.fetch(start_cursor=None, limit=self._query_limit)
        next_cursor = True
        while next_cursor:
            query_page = next(query_iter.pages)
            next_cursor = query_iter.next_page_token
            yield query_page
            query_iter = query.fetch(start_cursor=next_cursor, limit=self._query_limit)

    def _get_all_namespaces(self) -> List[str]:
        """
        Get all namespaces
        """
        if self._namespace is not None:
            return [self._namespace]

        all_namespaces = []
        for namespaces_page in self._query_pages(1, "__namespace__"):
            all_namespaces.extend(entity.key.id_or_name for entity in namespaces_page)
        return all_namespaces

    def _get_all_kinds_by_namespace(self, namespace: str) -> Iterator[str]:
        """
        Get all kinds of a namespace except kinds starting with '__', which are reserved ones.
        """
        for page in self._query_pages(namespace, "__kind__"):
            for entity in page:
                kind_name = entity.key.id_or_name
                if kind_name.startswith("__"):
                    continue
                elif self._kind and (self._kind != kind_name):
                    continue
                else:
                    yield kind_name

    def _delete_all_entities_by_kind(self, namespace: str, kind: str) -> None:
        """
        Delete all records by their namespace and kind.
        """
        for entities_page in self._query_pages(namespace, kind, self._query_filters):
            entities_page = list(entities_page)
            self._client.delete_multi(entities_page)
            self._deleted_records_amount = self._deleted_records_amount + len(entities_page)

    def clean_up_datastore(self):
        """
        Clean all records up in Datastore.
        """
        print(f"Starting cleaning-up Datastore of project '{self._client.project}'")
        for namespace in self._get_all_namespaces():
            for kind in self._get_all_kinds_by_namespace(namespace):
                self._delete_all_entities_by_kind(namespace, kind)
        print(f"The number of deleted records: {self._deleted_records_amount}")
        print(f"Cleaning Datastore of '{self._client.project}' is finished ")


def main():
    parser = argparse.ArgumentParser(description="Clean-up Datastore")
    parser.add_argument("-p", "--project", type=str, required=True, help="Project ID")
    parser.add_argument(
        "-n",
        "--namespace",
        type=str,
        default=None,
        help="Optional. Datastore namespace",
    )
    parser.add_argument(
        "-k",
        "--kind",
        type=str,
        default=None,
        help="Optional. Datastore kind",
    )
    parser.add_argument(
        "-l",
        "--query-limit",
        type=int,
        default=QUERY_LIMIT,
        help="Optional. Query limit to Datastore",
    )
    parser.add_argument("--delete-all", help="Delete all Entities", action="store_true")
    parser.add_argument(
        "-q",
        "--query-filters",
        type=str,
        help="Query filter. Usage: -q '<field> <operator> <value>' -q '<filter2>'",
        default=None,
        action="append"
    )

    args = parser.parse_args()

    if not args.delete_all and not args.query_filters:
        raise argparse.ArgumentError(
            "You should specify either '--query-filter <query>/-q <query>' or '--delete-all' flag."
        )

    datastore_cleaner = DatastoreCleaner(
        project=args.project,
        query_filters=args.query_filters,
        kind=args.kind,
        namespace=args.namespace,
        query_limit=args.query_limit
    )
    datastore_cleaner.clean_up_datastore()


if __name__ == "__main__":
    main()
