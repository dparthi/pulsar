commit 434e8e35f356464317e3ca7d666a4d156ad4ff04
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Mon Jan 13 16:11:03 2020 +0530

    tried redis pubsub
    
    redis pubsub did not work; it gave good latencies but poor throughputs;
    instead using a thread sleep and retry for waiting on redis data
    availability

commit 34ccf26b57b264422647b6b041fcb160d17d97bb
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Fri Jan 10 17:23:20 2020 +0530

    creating a webservice with decent performance
    
    using pulsar as a backend tried to create a web service handler
    started with a simple echo server
    using pulsar to create dynamic topics is slow; the p90 values are in the
    1s ranage which is unacceptable
    using a redis server to connect the response back to the request
    handling thread speeds up the whole thing; p90 of 20ms is achievable;
    multiple consumers are used
    multiple producers are also tried
    an optimal number of producers and consumers should be found
    TBD
    - multiple partitions
    - redis pubsub instead of a infinite loop until the response is
    available

commit e6fdf29934fd6ace8750f5e9eb276d52a46f3f98
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Thu Dec 12 09:47:35 2019 +0530

    refactored main thread and the pulsar components
    
    separated consumer and producer code
    consumer made to listen to "txns" topic and respond to the client
    a hashmap is used for sharing client connection between consumer and producer
    consumer - runs as in independent thread; always on
    producer - runs in the http thread (one per request)
    single instances of consumer and producer are used

commit e0585a7e7b36a8692b6278b1d6981fc553d2b375
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Wed Dec 11 16:44:24 2019 +0530

    moved pulsar code out of http flow; testing barebones server; wrk sends an empty connection at the start

commit a449fbd3b2077603b7a3fe7f6a9b97dae89d2b7c
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Wed Dec 11 15:45:50 2019 +0530

    split the consumer and producer into two runners for testing

commit 1e121aae4ae9fc23ee0cf6cf6c8c79931d5631ec
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Wed Dec 11 13:16:06 2019 +0530

    added pulsar client producer and consumer and able to publish and receive data from pulsar

commit c7835e58226ba63470bd5295c1b6ad0f883d7364
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Wed Dec 11 10:19:52 2019 +0530

    removed unused sections of the server code

commit 03e065ba89d7ebd7037f35002eeb8903b2fe73e8
Author: Parthi <parthiban.duraisamy@juspay.in>
Date:   Wed Dec 11 10:07:09 2019 +0530

    project for pulsar evaluation
    
    the thought process is to create a java web server, use pulsar for all
    persistence and evaluate its behaviors and performance
    tasks are
    - create a java web server
    - each request will ask a producer to write a message to a topic created
    using an id that comes from the request data
    - each response will be created by a consumer reading from a topic
    created by using the same id
