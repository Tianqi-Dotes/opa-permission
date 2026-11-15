# ABAC Management Platform — Presentation Script


Hi everyone. Today, I'd like to introduce the ABAC Administration Point,  
a new management platform.  
I'll walk you through how it works and explain the logic behind it.  
Here's what I'll cover today: what problem this administration point solves,  
an end-to-end demo for each resource type,  
and the roadmap ahead.

## 1. Why I Built This Platform

First, why do we even need a management platform?  
OPA is a powerful policy engine, but writing and understanding a policy is still very manual.  
OPA is meant to control access to a resource, yet a policy stays completely decoupled from the platform resource.  
In the open-source OPA ecosystem, there isn't really a tool that pulls in a platform resource and lets you set a policy on top of it.  
So I built this platform to bridge that gap between the platform resource and a proper administration solution.  
The goal is to move away from editing a config file by hand,  
and give an admin one place to configure and review every resource, attribute, and policy.  
Then, using GitOps, OPA can dynamically pick up any policy change.


## 2. The Solution and Responsibilities

Next, let's look at the solution. My approach isn't to replace OPA.  
It's to add a management layer in front of it. OPA stays as our ABAC decision point.  
Here's how the old workflow looks: a maintainer has to edit an OPA policy file by hand,  
and OPA evaluates each incoming request against that file at runtime.  
Now, here's the new workflow. Take Data PaaS as an example. We start by generating an attribute value directly from a user action.  
For instance, when you assign a role, we can automatically generate a default subject attribute.  
And when someone creates a bucket, we can automatically write a default resource attribute value like `tenant_id` or `group`.

Once that foundation is ready, the platform ties it together into a condition, rule, and policy,  
generates the policy file, and lets OPA load the update dynamically.

Put simply, the platform handles how we configure and maintain a policy around each business resource.  
It acts as the administration point,  
while OPA continues to do what it does best: making a decision based on the policy and the request.

## 3. Review Basic Concept from the Last Session

Let's do a quick recap of ABAC.

## 3. Hybrid Control

TODO

## 4. How the Platform Organizes Attribute and Rule Data

With that in mind, let's get back to the management side.  
The next step is organizing the data a policy relies on.  
First, RBAC is pretty straightforward. We assign a role to a user,  
and each role maps to API access,  
which controls whether a user can reach a specific API or feature.

For ABAC, we start with the Attribute Dictionary.  
It standardizes each attribute name, category, and data type,  
giving us an attribute key for any subject, resource, or action.  
We have a built-in default for each category, like `tenant_id` and `group` for a subject,  
and `resource_type` and `tenant_id` for a resource.

Moving to the next menu, Subject Attribute shows every user in the current tenant along with their attribute data,  
and we can also bind an attribute manually.  
Resource Attribute works the same way, just for managing each resource attribute.

Next is a key concept: Attribute Template.  
A template defines the mapping between a user action and an attribute.  
Obviously, we can't configure every single attribute by hand for every user or resource.  
So I believe we need a default platform template that automatically generates an attribute value when a user takes a certain action.  
That's what powers the Assign Role and Create Bucket flow I mentioned earlier, cutting down on manual setup.  
For example, if we assign Rick the Analyst role,  
and switch back to Subject Attribute, we'll see that this action automatically generated a `tenant_id` and `role` value for Rick.  
Along with the built-in platform template, a user can also create a tenant-level template  
to define a custom mapping between an action and an attribute.

Next, let's see how a policy is generated and validated through an internal and an external example.

## 5. Internal vs. External Resource and Decision Flow

Once the attribute data is ready, we need to define which resource a policy applies to.  
Right now, the platform separates any resource into Internal or External.

An Internal Resource is managed by the Data PaaS backend, like a bucket.  
The backend handles bucket provisioning itself.  
The key thing here is that when a user tries to access a bucket,  
the backend acts as the enforcement point and builds the request to OPA.  
That makes it super flexible. The request can take whatever shape we want and include any info, with no constraint.

An External Resource is a bit trickier. Here, we're using Trino connected to an external PostgreSQL data source,  
and we can access that Postgres database through our backend.  
The fundamental difference is that when someone queries a Postgres table,  
Trino is the enforcement point, not our backend.  
Trino builds the decision request itself, and the schema between Trino and OPA is fixed.  
By default, the subject only includes the user name, or group info if you add a plugin.  
For ABAC, though, we need much richer, more flexible subject context, including a custom attribute.  
So here's how I connected them: both Trino and OPA are wired up with token authentication.  
We can pack the subject's dynamic attribute data right into the JWT claim payload.  
When a user queries a table through the backend, the backend puts that dynamic attribute data into the token,  
and Trino forwards that token to OPA as part of the decision request.  
Since both Trino and OPA have the key to verify and read the token,  
OPA gets full access to every dynamic subject attribute.

Now let me walk through an internal and an external demo  
to tie together everything we just talked about, the resource, attribute, rule, and decision flow.  
First is an Internal mock: we'll create bucket A and bucket B, and then provision a pipeline,  
to verify an allow/deny policy for accessing a resource within the same tenant.  
Second is an External Trino demo: we'll generate a row-filtering policy and a column-masking policy,  
and see how the query result differs for each user.

## 6. Demo: From Resource to Policy

### Internal: Tenant Isolation

Let's start with the Internal Resource demo. We'll create bucket A and bucket B,  
use them to provision a pipeline, and retrieve a service account.  
For this case, we'll set up a default allow/deny policy  
that lets a user read any resource in their own tenant.

First, let's mock-create bucket A and bucket B.  
If we check Resource Attribute, we can see that creating each bucket  
automatically wrote the default attribute value onto it.

Now comes the main part, how a policy is created, published, and put into effect.  
We'll create a single policy covering every bucket so a user can read any bucket in the same tenant.  
This policy has a three-part condition:  
first, `resource_type == bucket`;  
second, `action == read`;  
and third, `subject.tenant_id == resource.tenant_id`.  
Because it compares that attribute data dynamically, we don't have to write a separate policy for every tenant ID.

Next, let's publish it. The config change is written to the local policy file.  
We run a command to build the bundle and push it to GitHub, which handles versioning.  
Meanwhile, if we try creating the pipeline right away, it should return deny while we wait for OPA to pick up the update.  
Once the ETag shows up, that tells us OPA has loaded the new policy.  
Let's try creating the pipeline again. Now each resource decision returns allow,  
and it switches to the service account.  
If we inspect the request body, we can see the resource and action info right there,  
while the subject info sits in the JWT.  
Once everything is allowed, the backend looks up the matching service account.

### External: Trino Row Filtering and Column Masking

Now let's switch to the External Resource demo.  
First, we'll create a new Trino data source.  
Then we hop over to the Query page. Since the default policy is read-only, querying the `customers` table shows us all the data.  
Before we configure a new policy, we need to dynamically pull in its current structure.  
So we go to Resource Attribute, select External, and load the catalog, schema, table, and column tree.  
We'll set `classification = 3` on `customer_name`,  
which saves this Trino resource tree to the backend.

Now let's create a row-filtering policy at the `customers` table level.  
We don't need any condition here. It applies to everyone.  
The expression matches `country_code` against the user's `region`,  
so each user can only query data from their own region.

Next, let's add masking.  
We head back to the policy screen,  
pick the `customer_name` column as our resource, and apply masking to it.  
The condition is `subject.clearance == 3`. When that matches, the customer name gets masked.  
Then we follow the same publish flow: we can see the local file changed,  
so we package it and push it to GitHub.  
To save a little time, let's just restart OPA so it loads right away.

Now let's try the query again.  
For the `admin` user, whose clearance is 3, `customer_name` is masked,  
and we only see customer data from `CN`.  
If we log in as Rick instead, there's no masking,  
and the exact same SQL returns customer data from `GB`.

## 7. Policy Preview, Publishing, and Dynamic Loading

In both demos, you just saw how a policy is generated in the management platform  
and published out to OPA in real time.

## 8. Roadmap and Summary

This is still an early version, and here is what I plan to improve next.

First, exploring how an agent can draft a policy from natural language,  
while keeping validation and human review in place before publishing.

Second, improving synchronization and policy scope check when an External Resource structure changes.  
For example, when a table or column changes in Trino,  
we need to identify whether any existing policy is impacted.

Third, defining precedence between a Tenant-level and a Project-level policy:  
when a Tenant configures a policy on a specific resource, how should that Tenant decision take effect,  
and when no Tenant policy applies, how should we fall back to the Project policy?

Fourth, defining how the default read-only policy combines with any other policy,  
including conflict priority and how an allow and a deny decision work together.  
That policy behavior still needs further design and validation.

Fifth, integrating this workflow and each building block with the Data PaaS control plane.

To wrap up: I designed and built an ABAC administration solution for Data PaaS,  
adding a policy management layer right between the business platform and OPA.

It connects each business resource, attribute, and policy.  
Through one unified interface, an admin can read platform data, configure and review a policy,  
generate and preview a policy file, and manage each release with GitOps.  
OPA continues to evaluate the policy, while the business system enforces the decision.

With this in place, a policy isn't just an isolated Rego file anymore.  
It can be managed right alongside each platform resource,  
making any change much easier to review and maintain while cutting down on manual policy writing.  
I hope this open-source exploration gives Data PaaS an extensible foundation  
for fine-grained access control going forward.

Thank you!
