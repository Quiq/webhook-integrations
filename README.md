# Web Hook Integration with the Centricient Platform
The Centricient Platform supports registering an external web hook that will get called as different events occur within the system. This is a helpful feature as it allows you to write custom integrations that do things such as writing all conversation related data to your own backend system when ever a conversation's status changes.

### Requirements and Security
Centricient supports and requires encrypted communication to any external web hooks (HTTPS). When you register your URL with Centricient we will give you a secret token. When we post to your webhook Centricient will send a header with the id of "X-Centricient-Hook-Token" and a value of this secret token. You can use this to verify that header to verify that it is in fact Centricient posting to your endpoint.

### Web hook Payload
Whenever Centricient calls your registered web hook we will pass JSON to you in the following format:
```
{
    id: (guid - unique id for every event we post to you),
    eventType: (Test, ConversationStatusChanged, ...)
    data: Object specific to they type of event (see below)
}
```

_Note: The ID will be unique for each unique event in the system. On rare occasions you may receive a call to your webhook with a duplicate payload as a previous call. You can use this ID to verify that this is in fact a duplicate message you are receiving. These scenarios can occur for example if we publish to your endpoint and don't receive a timely response because of network issues. We will then republish the message in order to make sure you've received it._

#### Test Event Type
When the test web hook API is called Centricient will post to your registered web hook with an event type of Test. It is expected that you will respond with a valid 200 response code. 

**Example Json:**
```json
{
   "eventType":"ConversationStatusChanged",
   "id":"245bc2f9-414e-4f49-a296-dc5e50d85292",
   "data":{}
}
```

#### ConversationStatusChanged Event Type
Whenever a conversation status changes we will call your webhook with this event. This includes when a new conversation is created, when a conversation is made inactive or reactivated, and when a conversation is closed. The data property of the event will contain a conversation object.

**Conversation Object**

| Property | Description|
|---|---|
|id | String: Conversation ID|
|owner | String: Who the conversation is currently assigned to - can be empty if not currently assigned|
|status | ConversationStatus: new, active, inactive, closed 
|collaboration | Conversation: Nested conversation object if there was a collaboration |
|messages | List of Messages: Messages in the conversation|
|metrics | ConversationMetrics: Nested metrics object|
|integrationsData| Mapping of custom integrations data (INTEGRATION_ID -> INTEGRATION_VALUE) |

**ConversationMetrics Object**

| Property | Description |
|---|---|
|startTime | Long: Time conversation started (Unix epoch time in milliseconds)|
|endTime | Optional Long: Time conversation ended (Unix epoch time in milliseconds) |

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
| assetId | Identifier of aasset |
| contentType | Type of content (such as image/png) |
| oneTimeUrl | Url that can be used once to fetch the asset |


**Example JSON:**
```json
{
   "eventType":"ConversationStatusChanged",
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
                  "oneTimeUrl":"https://inbound.centricient.com/web/assets/onetime/08af0f24-a59a-4eef-bfe2-29812168ed47",
                  "contentType":"image/png",
                  "assetId":"6a3d60fe595901e9aecc2cc0f740daec0a79e124701f57fe36e315548cf6c3e5"
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
      "integrationsData":{"Some-Integration-Provider": "Some custom data",
        "Other-Integration-Provider": "Other cusotm data"
      },
      "collaboration":null,
      "id":"a918f237-88f4-44cc-9072-84e2880e3b7d",
      "status":"active",
      "metrics":{
         "startTime":1468865667018,
         "endTime":null
      },
      "owner":"some-agent1"
   }
}
```

### In case of errors
The Centricient Platform gracefully handles when an error occurs while calling an external web hook. These errors can occur because your hook returns a non 200 level HTTP response or a timeout occurs trying to call your hook. In either of these scenarios we will retry a few seconds later and continue retrying with an exponential back off. We will also send all of the site admins an email when an error first occurs and periodically after that. Once the errors quit occuring all events that were published while the error occurred will be passed to the webhook.

### Developer tips
* **Running/Debugging behind private firewall**: If you are running on a development box behind a firewall and don't have a way to route traffic from a public URL we suggest checking out [ngrok](https://ngrok.com) as a simple (and free) tool to route traffic in this scenario. 

### Sample Code
We have provided some sample code in different technologies so you can easily see how to integrate. If you are working in a technology we don't yet have a sample for we suggest you check out the [Python Sample](samples/python) as it is very straightfoward to understand and translate into your given techonology stack.
* [Python Sample](samples/python) - Uses python and flask to host a self-contained microservice integration
* [Java Sample](samples/java) - Uses java and the Dropwizard framework to host a self-contained microservice integration
