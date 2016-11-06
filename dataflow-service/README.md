# README

* go to [the Spring Cloud Data Flow service](http://localhost:9393/dashboard)  and then click on [STREAMS](http://localhost:9393/dashboard/index.html#/streams/definitions)
* click `Create Stream` and create a solution that monitors file's and publishes the information to the same Spring Cloud Stream `reservations` destination:
  ```
file --directory=/Users/jlong/Desktop/in --filename-pattern=*.txt --mode=lines | transform --expression=payload.toUpperCase() > :reservations
```
 *Note* that the directory needs to reflect your home directory, not mine!
* make sure to click *Deploy* when you create the stream or click it in the *Streams* dashboard
* Once the deployment state changes to `Deployed`, make sure you have a directory on the desktop called `in`.
* then create a text file called `in.txt` and, in the text file, add a new name in lowercase, each on its own line, like this:
 ```
dave
spencer
brian
sebastien
michelle
mia
phil
jennifer
..
 ```
* now, visit the `http://localhost:9999/reservations/names` endpoint and confirm that those new names are added to the returned values, upper-cased'd.
