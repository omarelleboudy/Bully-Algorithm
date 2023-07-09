## Bully Algorithm
A small implementation for the Bully Algorithm using a socket server that would receive and send the states between the processes.

## What is the Bully Algorithm?

In distributed computing, the bully algorithm is a method for dynamically electing a coordinator or leader from a group of distributed computer processes. The process with the highest process ID number from amongst the non-failed processes is selected as the coordinator.

# The algorithm uses the following message types:

Election Message: Sent to announce the election.
Answer/Acknowledge (Alive) Message: Response to the Election message.
Coordinator (Victory) Message: Sent by the winner of the election to announce victory.
When a process P recovers from failure, or the failure detector indicates that the current coordinator has failed, P performs the following actions:

If P has the highest process ID, it sends a Victory message to all other processes and becomes the new Coordinator. Otherwise, P broadcasts an Election message to all other processes with higher process IDs than itself.
If P receives no Answer after sending an Election message, then it broadcasts a Victory message to all other processes and becomes the Coordinator.
If P receives an Answer from a process with a higher ID, it sends no further messages for this election and waits for a Victory message. (If there is no Victory message after a period of time, it restarts the process at the beginning.)
If P receives an Election message from another process with a lower ID it sends an Answer message back and if it has not already started an election, it starts the election process at the beginning, by sending an Election message to higher-numbered processes.
If P receives a Coordinator message, it treats the sender as the coordinator.
