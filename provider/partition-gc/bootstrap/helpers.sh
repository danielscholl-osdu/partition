#!/usr/bin/env bash

merge() {
  local system_data_function_name="$1"
  local additional_data_function_name="$2"
  local json1
  local json2
  
  json1="$($system_data_function_name)"
  json2="$($additional_data_function_name)"
  
  jq -n --argjson json1 "$json1" --argjson json2 "$json2" \
    '$json1.properties + $json2.properties | { properties: . }'
}

