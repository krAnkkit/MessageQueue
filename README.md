# MessageQueue

## Problem statement
Develop a message queueing system.
Functional requirements of this system have been described below.
1. Create your own queue that will hold messages in the form of JSON(Standard
   library with queue implementation is not allowed).
2. There can be more than one queue at any given point of time.
3. There will be one publisher that can generate messages to a queue.
4. There are multiple subscribers that will listen to queues for messages.
5. Subscribers should not be tightly coupled to the system and can be added or
   removed at runtime.
6. When a subscriber is added to the system, It registers a callback function which
   makes an API call to the given end point with the json payload, this callback
   function will be invoked in case some message arrives. 

Bonus : There must be a retry mechanism for handling error cases when some exception
   occurs in listening/processing a message, that must be retried.

## How to use
### Instantiating a new MessageQueue
    MessageQueue mq1 = QueueManager.get("mq1"); //Name of messagequeue (global scope)

### Adding an endpoint subscription
    mq1.subscribe("http://your.custom.domain.accepting.json.payload"); //http only for now
_Any messages published before this subscription will not be sent to this endpoint, even if delivery of those messages has failed and is currently being retried._

### Publishing a message
    mq1.publish("{'a':'a'}"); // sending a JSON string works. Throws MessageException if malformed json is sent
_If a message is successfully delivered to atleast one subscriber, it is removed from the queue after attempting delivery to other subscribers. Default behavior is to retry delivery 3 times after first failure with exponential backoff._

### Unsubscribing an endpoint
    mq1.unsubscribe("http://your.previously.subscribed.endpoint"); // Nothing bad happens if a not-yet subscribed or malformed string is sent
_No new messages will be delivered after this unsubscription, but the ones already in flight will not be canceled. If a message delivery failed and is set to be retried in a few seconds/minutes, that message delivery will be attempted even after this endpoint is unsubscribed here._

### Sample code : [MessageQueueDriver](https://github.com/krAnkkit/MessageQueue/blob/main/java/com/navi/driver/MessageQueueDriver.java)
    MessageQueue q = QueueManager.get("q8");
    q.subscribe("http://google.com");
    q.publish("{'a':'a'}");
    q.subscribe("https://ptsv2.com/t/r1wo9-1626329942/post");
    q.unsubscribe("abc");
    q.publish("{'b':'b'}");
    q.unsubscribe("http://google.com");
    q.publish("{'d':'d'}");

## Not supported
1. Custom retry config per subscription
2. Expiry time per message
3. Default time-to-live for all messages in queue
 