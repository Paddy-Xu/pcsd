local <- read.table("./results_local.csv", sep=",", header=TRUE)
server <- read.table("./results_server.csv", sep=",", header=TRUE)
pdf("throughput.pdf")
throughput = c(local$throughput/max(local$throughput),
               server$throughput/max(server$throughput))
plot(0, 0, xlim=c(0, 11), ylim=c(min(throughput), 1), type="n",
     xlab="# of worker threads [1]",
     ylab="Normalized throughput [Successful interactions/second]")
lines(local$workers, local$throughput/max(local$throughput), type="b", col="firebrick", pch=3)
lines(server$workers, server$throughput/max(server$throughput), type="b", col="royalblue", pch=4)
pdf("latency.pdf")
latency = c(local$latency/max(local$latecy),
            server$latency/max(server$latemcy))
plot(0, 0, xlim=c(0, 11), ylim=c(min(latency), 1), type="n",
     xlab="# of worker threads [1]",
     ylab="Normalized latency [Seconds/successful interaction/worker]")
lines(local$workers, local$latency/max(local$latency), type="b", col="forestgreen", pch=3)
lines(server$workers, server$latency/max(server$latency), type="b", col="tomato", pch=4)
