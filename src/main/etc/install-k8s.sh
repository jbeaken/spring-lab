helm install redis stable/redis --set password=margarine
kubectl create --filename service-dos/configmap.yml
kubectl create --filename service-dos/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename service-uno/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename service-tres/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename cloud-gateway/target/classes/META-INF/dekorate/kubernetes.yml
kubectl create --filename httpbin/target/classes/META-INF/dekorate/kubernetes.yml
