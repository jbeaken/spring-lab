# Building The Containers

Each project contains a Dockerfile that will create a container for the app.  You can do this easily
by running `./mvn clean package -Pdocker`.

**NOTE** You should edit the parent POM file and change the value of `docker.image.prefix` to
your own prefix.

After building the container images you will need to push them to a repository that your Kubernetes
deployment has access to, for example Docker Hub.

# Deploying To Kubernetes 

Each project uses [Dekorate](https://github.com/dekorateio/dekorate) in order to build a YAML file that can be used to deploy the app to Kubernetes.

To create the YAML files all you need to do is run `./mvnw clean package`.  The YAML files are located in
`/<module>/target/classes/META-INF/dekorate`.  


```
helm install redis stable/redis --set password=margarine
kubectl create --filename service-dos/configmap.yml
kubectl create --filename service-dos/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename service-uno/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename service-tres/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename cloud-gateway/target/classes/META-INF/dekorate/kubernetes.yml
```


## Testing The Apps

Each app is exposes a `nodeport`, you will need to find the `nodeport` for each application in your deployment.
If using MiniKube you can easily open the service by running `minikube service <servicename>`.  For example

```
$ minikube service gateway
```

