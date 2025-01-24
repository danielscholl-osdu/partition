package org.opengroup.osdu.partition.util;

public class Constants {
    public static final boolean EXECUTE_AUTHORIZATION_DEPENDENT_TESTS = Boolean.parseBoolean(System.getProperty("EXECUTE_AUTHORIZATION_DEPENDENT_TESTS",
            System.getenv("EXECUTE_AUTHORIZATION_DEPENDENT_TESTS")));
}
