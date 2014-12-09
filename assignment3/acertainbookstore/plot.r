local <- read.table("./results_local.csv", sep=",", header=TRUE)
# server <- read.table("./results_server.csv", sep=",", header=TRUE)
pdf("throughput.pdf")
plot(local$workers, local$throughput, type="b",
     xlab="# of worker threads [1]",
     ylab="Throughput [Successful interactions/second]", col="firebrick", pch=3)
# plot(server$workers, server$throughput, type="b", col="maroon", pch=4)
pdf("latency.pdf")
plot(local$workers, local$latency, type="b", xlab="# of worker threads [1]",
     ylab="Latency [Seconds/successful interaction/worker]", col="royalblue",
     pch=3)
# plot(server$workers, server$latency, type="b", col="orchid", pch=4)
