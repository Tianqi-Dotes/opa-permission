# ABAC Management Platform — 分段演讲稿

建议时长：8–10 分钟，含演示。括号内为操作提示，不必照读。
各位好，今天我给大家介绍ABAC管理平台，以及演示工作模式和解析背后的工作逻辑。
今天的演讲涉及以下几个部分：todo

## 1. 为什么做这个平台

首先，我会说明为什么需要管理平台？
OPA是一个强大的策略评估引擎。但策略的创建和理解都依赖手工处理。
OPA本身是做资源控制的，但POLICY和平台resource却是隔离的。开源OPA生态没有一个工具来拉取平台资源，并设置权限策略。
我开发这个平台的目的是弥补平台资源和administration solution的空缺，
是想改变手工维护配置这个模式，让资源、属性和策略能通过统一的管理界面来配置和审阅，
然后通过gitops，让OPA动态加载policy变更。


## 2. 解决方案与职责边界

接下来介绍解决方案。我的做法不是替换 OPA，而是在 OPA 前增加一个管理层。OPA依然是ABAC中的decision point。
这个是原来的工作模式，maintainers需要手工维护OPA的policy文件，OPA在运行时根据请求和策略做出决策。
这个是新的工作模式，比如说在data paas中，先从用户行为产生一些attribute value。
比如说在assign role的时候，我们可以默认生成一些subject的attribute。
比如说在create bucket的时候，可以默认给resource写入一些tenant_id,group的attribute。

有了这些基础元素之后，管理平台可以把它们组织起来，形成rule，condition和policy，并生成policy文件最终OPA动态加载policy的变化。

换句话说，管理平台解决的是“如何结合业务资源配置和维护策略”，管理平台是administration point，而 OPA 继续负责“根据策略和请求作出什么判断”。

## 3.review basic concept from last session
我们再来回顾一下ABAC的基础概念

## 3. Hybrid Control
todo

## 4. 平台如何组织属性与规则

有了这些概念，我们回到management。下一步是把策略需要的信息组织起来。
###首先是RBAC，RBAC比较简单了，我们可以给用户分配角色，角色对应一系列的API权限。决定一个用户能不能访问某个API和功能。

关于ABAC首先提供 Attribute Dictionary，也就是属性字典，统一定义属性的名称、类别和数据类型,类别可以给resource,subject,action准备attribute key。
这里有一些默认的设置，比如说subject的tenant_id和group。Resource的resource_type和tenant_id。

下来这个菜单，Subject attribute可以看到当前租户下的所有用户，以及他们的attribute，也可以人工捆绑属性，
Resource Attribute 同样维护的是resource的属性。

下来是一个重要的概念。Attribute Template 是属性模板，定义了用户行为和attribute之间的关系。
因为不可能为每个资源或用户人工配置所有属性。我认为需要默认提供平台默认模板，在用户行为中产生默认的attribute value。
这也是前面所提到的assign role和create bucket的例子。
默认属性模板可以在资源创建或用户注册时自动写入属性值，减少手工配置的工作量。
比如说，我们去给RICK assign一个analyst的role，回到subject attribute可以看到，通过这个行为，对RICK产生了tenant_id和role的属性。
这里会有platform提前定义好的template，用户可以创建tenant level的template，自己定义行为和属性之间的关系。

下来就是policy的生成和验证相关的内容。我们通过实例来了解。

## 5. 两类资源与决策链路

属性准备好之后，还需要明确策略作用于哪些资源。目前平台区分 Internal 和 External 两类资源。

Internal Resource 是由应用data paas后端管理的资源，例如 bucket。后端本身包含了bucket provisioning的过程。
根本在于，在这个case下，用户去请求访问某个bucket，backend是enforcement point来组装OPA的请求。所以是super flexible的。
request可以是任何格式，包含任何信息，没有约束。

External Resource就复杂一点。 当前以 Trino 为例，外挂了一个pg的数据源。
我们可以通过后端去访问这个PG。
external与internal的根本区别在于，在用户访问pg某个表的场景下，trino是enforcement point。
也就是说是trino去组装decision的请求，trino和OPA的模式是固定的，默认subject只包含subjectName信息，还可以用插件支持group。
但是ABAC本身应该subject信息更灵活。
所以这里是一个新的连接方式，trino和OPA都打通了token认证。我们可以把subject的动态属性都写在claims里。
当用户在后端查询表时，后端会把subject的动态属性写在token里，trino在组装OPA请求时，会把这个token传递给OPA。
trino和OPA都拥有密钥去解析token，从而OPA可以拿到动态的subject属性。

接下来我用两个例子，
把刚才介绍的资源、属性、规则和决策链路串起来。
一个是internal的mock，先创建bucket，再去创建pipeline的例子。验证一个默认用户可以访问同租户下的资源的allow/deny policy.
一个是external的trino，会生成row filtering和column masking的policy。并验证不同用户看到的结果。

## 6. 演示：从资源到策略

### Internal：租户隔离

首先看 Internal Resource，我们会创建2个bucket，并基于两个bucket创建一个pipeline并读取sa。
这个案例会创建一个默认的allow/deny policy，允许用户读取同租户下的资源。

我们先去mock创建两个bucket A,B。
我们可以看到resource attribute，通过创建bucket的行为，默认value写到了对应的BUCKET里。

下来是一个重点，看看policy是怎么产生，发布和生效的。
然后我们对all bucket生成一条policy，让所有用户都可以读取同租户下的bucket。
这个policy有三个condition。 
第一个是，resource_type==bucket.
第二个是action=read.
第三个是subject.tenant_id == resource.tenant_id。
这个是一个动态的验证，不需要针对不同的tenant_id去写不同的policy。

然后我们进行发布，配置变更写到了本地文件里。
我们使用指令生产bundle并提交到github。GITHUB负责版本的维护。
同时可以先尝试创建一下pipeline，应该是返回deny的，我们等一下OPA的动态加载。
当出现etag的时候，就代表他加载了新的策略。
我们再去创建pipeline，应该是返回allow的。并且置换到了sa。
这里我们可以查看一下请求体，里面描述的是resource/action的信息，subject的信息在jwt里。
all allow，那么backend查找对应的sa。

### External：Trino 行过滤与列掩码

再看 External Resource。
默认策略是全部只读的，我们跑customer表可以看到所有数据。
我们去生成一条rowFlitering的policy，resource是customer表级。
不需要condition，面向所有用户生效。
expression是country_code等于用户的region，每个人只能查自己region的数据。

再搞一个masking，
在开始之前，我们需要去动态拉取他当前的结构。//todo
然后我们回到policy界面。
resource是一个字段，customer name。我们对这个字段进行masking。
condition是subject.clearance=3，则mask用户名。
OK，一样的流程去发布。可以看到本地文件变化了，我们打包后发布到github去。
这次直接重启，让他去加载节约时间。
可以看到admin这个用户clearance是3，customer name有masking，同时只能查到CN的用户。
我们登录RICK，可以发现没有masking，同样的sql能查到GB的用户。

## 7. 策略预览、发布与动态加载

刚才的例子中，已经展示了策略在管理平台生成，同时实时发布给OPA。

## 8. Roadmap 与总结

目前这还是一个初版，接下来我计划继续完善几个方向。

第一，探索使用 Agent 根据自然语言生成策略草稿，并在发布前保留验证和人工审核。

第二，完善 External Resource 在结构变化后的同步和策略范围校验。例如，当 Trino 中的表或字段发生变化时，需要识别已有策略是否受到影响。

第三，明确 Tenant 和 Project 层级策略的优先级：当某个 Tenant 在特定资源上配置了策略时，如何让 Tenant 决策生效；没有适用的 Tenant 策略时，又如何回退到 Project 策略。

第四，定义默认只读策略与其他策略的合并方式，包括规则冲突时的优先级，以及允许与拒绝如何组合。这些策略语义仍需要进一步设计和验证。

最后总结一下：这个项目在 OPA 定义了一个administration point solution，
可以把data paas业务资源、属性和策略配置连接起来，并支持生成、预览和发布策略文件。

谢谢大家。
