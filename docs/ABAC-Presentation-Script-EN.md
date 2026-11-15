# ABAC Management Platform - Presentation Script

Suggested duration: 8-10 minutes, including the demonstrations. Instructions in parentheses do not need to be read aloud.  
Hello everyone. Today, I will introduce the ABAC management platform,  
demonstrate how it works, and explain the logic behind it.  
Today's presentation covers the following topics: TODO

## 1. Why I Built This Platform

First, why do we need a management platform?  
OPA is a powerful policy evaluation engine. But creating and understanding policies requires manual work.  
OPA controls access to resources, but its policies are disconnected from platform resources.  
There is no tool in the open-source OPA ecosystem to retrieve platform resources and configure permission policies.  
I developed this platform to bridge the gap between platform resources and an administration solution.  
My goal is to move away from manually maintaining configuration  
and provide a unified interface to configure and review resources, attributes, and policies.  
Then, through GitOps, OPA can dynamically load policy changes.


## 2. The Solution and Its Responsibilities

Next, let me introduce the solution. My approach is not to replace OPA,  
but to add a management layer in front of it. OPA remains the decision point for ABAC.  
In the original workflow, maintainers manually manage OPA policy files,  
and OPA makes decisions at runtime based on requests and policies.  
In the new workflow, for example in Data PaaS, we first create attribute values from user actions.  
For example, assigning a role can generate some default subject attributes.  
Creating a bucket can also write default resource attributes, such as `tenant_id` and `group`.

With these building blocks in place, the management platform can organize them into conditions, rules, and policies.  
It then generates policy files, and OPA dynamically loads the policy changes.

In other words, the platform addresses how to configure and maintain policies around business resources.  
It serves as the administration point, while OPA continues to decide  
what should happen based on the policies and requests it receives.

## 3. Review Basic Concepts from the Last Session

Let's briefly review the basic concepts of ABAC.

## 3. Hybrid Control

TODO

## 4. How the Platform Organizes Attributes and Rules

With those concepts in mind, let's come back to management.  
The next step is to organize the information needed by our policies.  
First, RBAC is relatively straightforward. We can assign roles to users,  
and each role corresponds to a set of API permissions.  
These permissions determine whether a user can access an API or a feature.

For ABAC, the first building block is the Attribute Dictionary.  
It provides a consistent definition of attribute names, categories, and data types.  
The categories provide attribute keys for resources, subjects, and actions.  
There are some default definitions, such as `tenant_id` and `group` for subjects,  
and `resource_type` and `tenant_id` for resources.

In the next menu, Subject Attributes, we can see all users in the current tenant and their attributes.  
We can also manually assign attributes to them.  
Resource Attributes similarly manages attributes for resources.

Next is an important concept: Attribute Templates.  
A template defines the relationship between user actions and attributes.  
Since we cannot manually configure every attribute for every resource or user,  
I believe the platform should provide default templates  
that generate default attribute values when users perform certain actions.  
These are the Assign Role and Create Bucket examples I mentioned earlier.  
Default attribute templates can write attribute values automatically when a resource is created  
or a user registers, reducing the amount of manual configuration.  
For example, let's assign Rick the Analyst role.  
When we return to Subject Attributes, we can see that this action has created  
the `tenant_id` and `role` attributes for Rick.  
The platform provides predefined templates, and users can create tenant-level templates  
to define their own relationships between actions and attributes.

Next, let's look at policy generation and validation through some examples.

## 5. Two Types of Resources and Their Decision Flows

Once the attributes are ready, we also need to identify the resources the policies apply to.  
The platform currently distinguishes between Internal and External Resources.

Internal Resources are managed by the Data PaaS backend, such as buckets.  
The backend itself is responsible for bucket provisioning.  
The key point is that when a user requests access to a bucket,  
the backend is the enforcement point and constructs the request to OPA.  
This is very flexible: the request can be in any format and contain any information, without constraints.

External Resources are a little more complex. In this example,  
we use Trino with an external PostgreSQL data source.  
The backend can access PostgreSQL through Trino.  
The fundamental difference is that when a user accesses a PostgreSQL table,  
Trino is the enforcement point.  
Trino constructs the decision request, and the request format between Trino and OPA is fixed.  
By default, the subject contains only the subject name,  
although groups can also be supported through a plugin.  
ABAC, however, should allow more flexibility in subject information.  
So, here we use a new connection method: both Trino and OPA support token authentication.  
We can put the subject's dynamic attributes into the token claims.  
When a user queries a table through the backend, the backend puts those attributes into the token.  
Trino then passes the token to OPA when it constructs the request.  
Both Trino and OPA have keys to verify and read the token,  
so OPA can access the subject's dynamic attributes.

Next, I will use two examples  
to bring the resources, attributes, rules, and decision flows together.  
The first is an Internal mock: we create buckets, then create a pipeline.  
This demonstrates an allow/deny policy for a user accessing resources in the same tenant.  
The second uses External resources in Trino to generate row filtering and column masking policies,  
and shows what different users see.

## 6. Demonstration: From Resources to Policies

### Internal: Tenant Isolation

First, let's look at an Internal Resource. We will create two buckets,  
create a pipeline using them, and retrieve a service account.  
In this example, we will create a default allow/deny policy  
that lets users read resources in their own tenant.

Let's start by using the mock to create two buckets, A and B.  
In Resource Attributes, we can see that creating the buckets  
has written default values to the corresponding resources.

Now, let's focus on how a policy is generated, published, and put into effect.  
We will generate one policy for all buckets, allowing users to read buckets in their own tenant.  
This policy has three conditions.  
The first is `resource_type == bucket`.  
The second is `action == read`.  
The third is `subject.tenant_id == resource.tenant_id`.  
This is a dynamic check, so we do not need to write a separate policy for every tenant ID.

Now let's publish it. The configuration changes are written to local files.  
We use a command to build a bundle and commit it to GitHub, which maintains the versions.  
Meanwhile, we can try creating a pipeline. It should return deny while we wait for OPA to load the update.  
When the ETag appears, it means OPA has loaded the new policy.  
Let's try creating the pipeline again. It should now return allow and switch to the service account.  
We can also inspect the request body: it contains the resource and action information,  
while the subject information is in the JWT.  
Once both checks return allow, the backend looks up the corresponding service account.

### External: Trino Row Filtering and Column Masking

Now let's look at an External Resource.  
The default policy is read-only, so we can query the customers table and see all the data.  
Let's generate a row filtering policy at the customers table level.  
It does not need a condition and applies to all users.  
The expression checks that `country_code` equals the user's `region`,  
so each user can only see data from their own region.

Next, let's add masking.  
Before we start, we need to dynamically retrieve the current structure. // TODO  
Then we return to the policy screen.  
The resource is the `customer_name` field, and we will mask that field.  
The condition is `subject.clearance == 3`; when it matches, the customer name is masked.  
Now we follow the same publishing process. We can see that the local file has changed,  
so we package it and publish it to GitHub.  
This time, we will restart OPA so it loads the policy and saves time.  
The admin user has a clearance of 3, so the customer name is masked,  
and the user can only see customers from CN.  
Now let's log in as Rick. We can see that the name is not masked,  
and the same SQL query returns customers from GB.

## 7. Policy Preview, Publishing, and Dynamic Loading

In these examples, we have seen policies generated in the management platform  
and published to OPA in real time.

## 8. Roadmap and Summary

This is still an initial version, and I plan to improve it in several areas.

First, explore using an agent to generate draft policies from natural language,  
while keeping validation and human review before publishing.

Second, improve synchronization when External Resource structures change  
and validate the scope of affected policies.  
For example, when a table or field changes in Trino,  
we need to identify whether any existing policies are affected.

Third, define policy precedence between the Tenant and Project levels.  
When a Tenant has a policy for a specific resource, how should the Tenant decision take effect?  
When no Tenant policy applies, how should we fall back to the Project policy?

Fourth, define how the default read-only policy combines with other policies,  
including precedence when rules conflict and how allow and deny decisions are combined.  
These policy semantics still need further design and validation.

Finally, to summarize: I designed and developed an ABAC administration solution for Data PaaS,  
adding a policy management layer between the business platform and OPA.

It connects business resources, attributes, and policies.  
Through a unified interface, administrators can read platform data, configure and view policies,  
generate and preview policy files, and use GitOps to manage policy releases.  
OPA continues to evaluate policies, while business systems enforce the decisions.

As a result, policies are no longer just independently maintained Rego files.  
They can be managed in the context of platform resources,  
making changes easier to review and maintain while reducing manual policy authoring.  
I hope this open-source exploration provides an extensible foundation  
for fine-grained access management in Data PaaS.

Thank you.
