import re, subprocess

server = subprocess.Popen("./run_server.sh")
with open("results.csv", "w") as outfile:
    outfile.write("workers,throughput,latency\n")
    for i in range(1, 11):
      print("Running for " + str(i) + " worker(s)...")
      throughput = 0.;
      latency = 0.;
      for j in range(10):
        process = subprocess.Popen("./run.sh " + str(i), stdout=subprocess.PIPE,
                                   shell=True)
        (output, error) = process.communicate()
        outputStr = output.decode()
        throughput += float(re.search(r"Throughput: *([^\n]+)",
                                      outputStr).group(1))
        latency += float(re.search(r"Latency: *([^\n]+)",
                                   outputStr).group(1))
      throughput /= 10.;
      latency /= 10.;
      outfile.write(str(i) + "," + str(throughput) + "," + str(latency) + "\n")
server.terminate()
