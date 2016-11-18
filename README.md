# Webhook Integration with the Centricient Platform
The Centricient Platform supports registering external webhooks to be called whenever certain events, such as a conversation closing, occur in the system. This enables you to write integrations that do things such as synchronizing conversation data to an external system.

### Requirements and Security
Centricient supports and requires encrypted communication to any external webhooks (HTTPS). When you register your URL with Centricient we will give you a secret token. When we post to your webhook, Centricient will send a header with the id of "X-Centricient-Hook-Token" and a value of this secret token. You can use this to verify that it is in fact Centricient posting to your endpoint.

### Webhook Payload
Whenever Centricient calls your registered webhook we will pass JSON to you in the following format:
```
{
    id: String (guid - unique id for every event we post to you),
    eventType: String (Test, ConversationRequested, ConversationActivated, ...)
    data: Object (Payload specific to they type of event (see below))
    timestamp: Long (Unix timestamp (in milliseconds) of when the event occurred
}
```

_Note: The ID will be unique for each unique event in the system. On rare occasions you may receive a call to your webhook with a duplicate payload as a previous call. You can use this ID to verify that this is in fact a duplicate message you are receiving. These scenarios can occur for example if we publish to your endpoint and don't receive a timely response because of network issues. We will then republish the message in order to make sure you've received it._

#### Test Event Type
When the test webhook API is called Centricient will post to your registered webhook with an event type of Test. It is expected that you will respond with a valid 200 response code.

**Example Json:**
```json
{
   "eventType":"test",
   "id":"245bc2f9-414e-4f49-a296-dc5e50d85292",
   "data":{}
}
```

#### Conversation Events
There are webhook events corresponding to the various state transitions that a conversation may experience. The state transitions are as follows:

* ConversationRequested - Occurs when a customer first messages your company
* ConversationActivated - Occurs when the conversation is first accepted by an agent and whenever the conversation is brought out of the inactive
* ConversationInactivated - Occurs when an agent (or the system timer) transitions a conversation to an inactive status
* ConversationClosed - Occurs when an agent (or the system timer) closes a conversation. This is a terminal event for the conversation.
* ConversationDeniedAsSpam - Occurs when an agent (or the system timer) marks a conversation as being spam. This is a terminal event for the conversation
* ConversationMergedAsDangling - Occurs when a requested conversation is later recognized as a continuation of a previous conversation and is 'merged' into that conversation. This is a terminal event for the conversation

Many integrations will only need the ConversationClosed event as that corresponds to an agent finishing a customer interaction. The data payload for the above events is a Conversation object:

| Property | Description|
|---|---|
|id | String: Conversation ID|
|owner | String: Who the conversation is currently assigned to - can be empty if not currently assigned|
|status | ConversationStatus: requested, active, inactive, closed, *deniedAsSpam*, *mergedAsDangling* |
|customerPlatform | MessagePlatform for customer: SMS, Facebook, etc. (N/A for collaborations) |
|collaboration | Conversation: Nested conversation object if there was a collaboration |
|events| A list of ConversationEvent objects that had occurred on this conversation at the time of the webhook event
|messages | List of Messages: Messages in the conversation|
|metrics | ConversationMetrics: Nested metrics object|
|integrationsData| Mapping of custom integrations data (INTEGRATION_ID -> INTEGRATION_VALUE) |
|startTime| The time the conversation started |
|endTime| The time the conversation ended, if applicable |

**ConversationMetrics Object**

| Property | Description |
|---|---|
|timeToFirstResponse | Optional Int: The duration, in milliseconds, between the customer's first message and an agent's initial response |
|averageResponseTime | Optional Int: The average duration, in milliseconds, between customer messages and agent responses |

**Message Object**

| Property | Description |
|---|---|
| id | String: Message ID |
| sourcePlatform | MessagePlatform: SMS, Facebook, Centricient, etc. |
| fromCustomer | Boolean: True if this came from a customer |
| author | String: Author of message<ul><li>Id of customer (such as phone # for SMS or facebook ID) if this is a customer message</li><li>Id of agent if not from a customer and an agent created the message</li><li>None if not from a customer and was an auto-response by the system</li> |
| text | String: Text of the message (can be none for asset only messages |
| assets | List of Assets: Media assets
| timestamp | Long: Time this message entered the Centricient system (Unix epoch time in milliseconds) |

**Asset Object**

| Property | Description |
|---|---|
| assetId | Identifier of asset |
| contentType | Type of content (such as image/png) |

*See [our public api](./PublicAPI.md) documentation for information on how to fetch assets*

**ConversationEvent Object**

| Property | Description |
|---|---|
| type | The type of conversation event. Current values are requested, activated, inactivated, closed, mergedAsDangling and deniedAsSpam |
| timestamp | Long: The time the conversation event occurred |
| triggeredBy | Entity object that identifies who triggered the event |

**Entity Object**

| Property | Description |
|---|---|
| type | String: The type of entity. Legal values are "customer", "user" and "system" |
| id | Optional string identifier for the entity. Currently only populated for "user" entities |


**Example JSON:**
```json
{
   "eventType":"ConversationClosed",
   "id":"245bc2f9-414e-4f49-a296-dc5e50d85292",
   "data":{
      "messages":[
         {
            "author":"+123456789",
            "assets":[
            ],
            "timestamp":1468865667018,
            "sourcePlatform":"SMS",
            "text":"hey",
            "fromCustomer":true,
            "id":"f5cdc154-e065-4ebc-8aed-0b9f2ee9ff1d"
         },
         {
            "author":"some-agent1",
            "assets":[
               {
                  "assetId":"6a3d60fe595901e9aecc2cc0f740daec0a79e124701f57fe36e315548cf6c3e5",
                  "contentType":"image/png"
               }
            ],
            "timestamp":1468865702716,
            "sourcePlatform":"Centricient",
            "text":"",
            "fromCustomer":false,
            "id":"9c2a148f-29b6-48f8-98e0-8a1321f4984e"
         },
         {
            "author":"some-agent1",
            "assets":[
            ],
            "timestamp":1468865758932,
            "sourcePlatform":"Centricient",
            "text":"hey",
            "fromCustomer":false,
            "id":"23e2c368-7a26-4906-b04f-c72cee049e07"
         }
      ],
      "integrationsData":{
        "Some-Integration-Provider": "Some custom data",
        "Other-Integration-Provider": "Other custom data"
      },
      "events": [
        {
          "type":"requested",
          "timestamp": 1468865667018,
          "triggeredBy": {
            "type": "customer"
          }
        },
        {
          "type":"activated",
          "timestamp": 1468865668018,
          "triggeredBy": {
            "type": "user",
            "id": "some-agent1"
          }
        },
        {
          "type":"closed",
          "timestamp": 1468865689023,
          "triggeredBy": {
            "type": "user",
            "id": "some-agent1"
          }
        },
      ],
      "collaboration":null,
      "id":"a918f237-88f4-44cc-9072-84e2880e3b7d",
      "status":"closed",
      "metrics":{
        "timeToFirstResponse": 30000,
        "averageResponseTime": 45000,
      },
      "owner":"some-agent1",
      "startTime":1468865667018,
      "endTime":1468865689023
   }
}
```

### Administration
We're still building out webhook administration from within our primary messaging app. In the meantime, we've put together
a basic hook administration page available at https://greatcompany.centricient.com/external/hooks/development, where
greatcompany.centricient.com is replaced by the domain you use to login to your Centricient site. You must be logged-in
to the primary messaging app while using the hook administration page.

### In case of errors
The Centricient Platform gracefully handles when an error occurs while calling an external webhook. These errors can occur because your hook returns a non 200 level HTTP response or a timeout occurs trying to call your hook. In either of these scenarios we will retry a few seconds later and continue retrying with an exponential back off. We will also send all of the site admins an email when an error first occurs and periodically after that. Once the errors quit occurring all events that were published while the error occurred will be passed to the webhook.

### Developer tips
* **Running/Debugging behind private firewall**: If you are running on a development box behind a firewall and don't have a way to route traffic from a public URL we suggest checking out [ngrok](https://ngrok.com) as a simple (and free) tool to route traffic in this scenario.

### Sample Code
We have provided some sample code in different technologies so you can easily see how to integrate. If you are working in a technology we don't yet have a sample for we suggest you check out the [Python Sample](samples/python) as it is very straightforward to understand and translate into your given technology stack.
* [Python Sample](samples/python) - Uses python and flask to host a self-contained microservice integration
* [Java Sample](samples/java) - Uses java and the Dropwizard framework to host a self-contained microservice integration

