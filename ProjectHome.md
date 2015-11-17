## _Minimalist distributed execution service using RMI_ ##

_Will spawn a grid of JVMs each running an Executor Service. Callables are submitted to a broker service and executed on the grid. Services are Springified and Callables are autowired via annotations. The service was designed for parallelizing the execution of JNI based compute engines._

This work is an evolution of thought and technology on distributed execution as a means of parallelization. It is influenced by earlier systems I created based on TCP/IP and JMS. These earlier systems were conceived by the need to find resources for real-time/intra-day compute engines. Distributed execution was the obvious means to re-use spare capacity on servers used for night-time batch based processing.

Although the Sockets based TCP/IP model was fast and efficient the codebase was large and complex. A re-write in the (new) JMS API reduced the size of the codebase and its complexity. It also produced significant benefits in terms of deployment, configuration and management - and all with an acceptable loss in performance.

The arrival of Lea's Concurrent Utils upped the game in terms of execution models and separation of concerns. The executor and the executable were now clearly separated. The execution logic was in one place. It was more easily developed and tested. Callable components were cleaner. The ExecutorService was a game changer. All applications are workflow. The ExecutorService is the foundation of workflow in Java. (The ExecutorService does away with writing your app in Autosys and having infinite nested while loops wait for messages in order to execute lots of if-else statements and the odd 'break'. This is true :)

With increased adoption of the Callable, the most obvious way to distribute computation is via the ExecutorService. You just (ceteris paribus) increase the threadpool size, but in another JVM (same or different physical machine). If the Callable needs data services then so be it. Inject them as access to a dedicated data grid.

In an ideal world all execution tasks would be Callables. They are discrete, testable and manageable components. They can be developed and tested on a developers machine. Whether they run in DEV, UAT, SIT or PROD is irrelevant - they should behave in the same way.

For this application I choose Spring RMI for the remoting. I chose it for several reasons:
  * I wanted to use RMI in order to get as close as possible to sockets for performance reasons.
  * The RMI model suits the application. It is not exposed. I get some added leverage over Sockets.
  * Spring RMI automated most of the crud and also provided a programmatic interface.

_There are many non-EJB systems out there that do distributed execution. There are data grids with execution/invocation services (GridGain, Coherence). There are dedicated compute grid systems that you can overlay with a data grid (DataSynapse). What I will say is that to use the data grid as an execution service or proxy ExecutorService is just plain wrong. Most developers, most of the time will develop the most awful systems that are illegible, unmaintainable and untestable if you give them this opportunity. Putting 'Listeners' on data caches just a more complex way of putting triggers on tables in a database. (... however, GridGain does have a more sophisticated execution service than other solutions). There is also the question of cost - not only the cost of licensing, but proprietary lock-in, development cost, bespoke knowledge and, again, the cost of picking the wrong tool for the job._

**_NB Download files are not current - please use SVN to download or Source/Browse to browse_**