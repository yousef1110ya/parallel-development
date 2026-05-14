# Parallel development class | e-commerce project 
this project is the final project for parallel development collage class for Damascus Uni , year 2025-2026 .
## the reason for the project 
the reason for this project is build our mind with the modern concepts of software development . where we are requested in this class to build an e-commerce with 2 ways . 
- the first way is to build the system in a normal way without optimizations to demonstrate the bottlenecks in the system . 
- the second way is to build the system in an advanced way by implementing concepts like ( async development , batch processing , load balancing .... ) . 
--- 
## the git workflow . 
### branches 
#### the main branches 
for this project to work right , we need to have 2 main branches : 
- dev-simple: this branch would be for the development in the simple way , with none of the modern concepts talked about before . 
- dev-final: this branch is the one with the advanced features . 
#### the development branches 
for each task there should be a new branch for that task and it should work as following : 
1. create a new branch with the namespace developername-task-taskname . 
2. create a PR to merge to dev-simple or dev-final based on the nature of the task . 
3. check for comments to check if there is any comments on the code as they won't be discussed directly .
### code review  
every pull request must be reviewd by yousuf or Omar to be approved  . 
### commit messages .
> please provide a full commit message not just something like fix bugs or added a feature . the commit message needs to be clear enough that the reader can know what you did and how you did it and what is the reason you choose to do what you did . 
--- 
## reports . 
as the project needs in-time reporting , this [google docs link](https://docs.google.com/document/d/1Lzil2X189-E8tpO5pyColt6u9bUnX4zLIuy3Jo1gnv4/edit?usp=sharing) will be the place where I add all  the reports step by step . 

---
## local running modes
- quick local run with in-memory H2:
  `mvn spring-boot:run`
- postgres-backed local run:
  `docker compose -f docker-compose.yml up -d`
  `mvn spring-boot:run -Dspring-boot.run.profiles=postgres`

the docker compose command only starts PostgreSQL. it does not make Maven use PostgreSQL automatically unless you activate the `postgres` Spring profile or provide the datasource environment variables yourself.

---
 ## Licence 
 IDC man , take this code as you please 
