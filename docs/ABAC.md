# PREREQUISITES:
ADMIN is ADMIN ROLE.
Has 3 members in the system: ADMIN, RICK, SAM.
Has 3 roles in the system: ADMIN, DATA-ANALYST, DEVELOPER.
Has 4 APIs: 1 query trino, 2 create trino catalog, 3 create trino schema, 4 create trino table
ROLE ADMIN has all permissions.
ANALYST has permission to query trino.
DEVELOPER has permission to create trino catalog, schema, and table.

# TEST CASE1: Assign DEVELOPER ROLE to RICK
1 Rick cannot access create trino catalog, schema, and table APIs.
2 Assign DEVELOPER ROLE to RICK.
3 Rick can access create trino catalog, schema, and table APIs.
4 Rick create catalog.schema.tableA

# TEST CASE2: Assign ANALYST ROLE to SAM
1 Sam cannot access query trino API.
2 Assign ANALYST ROLE to SAM.
3 Sam can access query trino API but cannot access tableA.
4 Assign ANALYST ROLE ABAC to catalog.schema.tableA.FIELD1,2,3.
6 Sam can access query trino API, but some field can't.