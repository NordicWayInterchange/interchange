# Interchange Helm charts

The helm charts in this folder is uploaded to the registry at oci://ghcr.io/nordicwayinterchange/interchange
The current version can be located by looking at the version listed in the chart file [here](https://raw.githubusercontent.com/NordicWayInterchange/interchange/federation-master/helm/interchange/Chart.yaml)

# Exposing

If you want to expose the application to the world, the IP's/ports for onboard-server, neighbour-server and qpid-secure
should be exposed. How this is done depends on the deployment/cloud used.


## Interchange Portal
The Interchange includes a simple portal implementation that uses both Auth0 and keycloak for authentication and letsencrypt for 
certificate management, as well as nginx-ingress as ingress. These components require the ability to install
components in separate namespaces. Additional requirements are a separate static IP for the portal, and an DNS A-record
for the domain name the portal is to be served from. We are moving toward using Keycloak as the only authentication provider
and access management solution going forward.

To enable the portal, first install [Niginx ingress](https://kubernetes.github.io/ingress-nginx/) and [cert-manager](https://cert-manager.io/)
into their separate namespaces. 
As the installation and configuration can be a bit involved, and that the actual config will change depending on the 
cloud provider/installation of Kubernetes,
make sure to review the documentation for the components and the cloud provider/installation option.
The nginx ingress might need a separate LoadBalancer in front, in that case, the setting `controller.extraArgs` need to
contain the setting `publish-service: ingress-nginx/<interchange release-name>-ingress-lb` 
Then, in order to install the portal, review the example values in the [example file](example_with_napcore.yml), and set the 
value of `napcore_frontend.enabled` to true.
It is advised to set the `letsencrypt.server` variable to the letsencrypt staging server until you are completely 
convinced that the setup is successful, so you don't wind up hitting the strict limits of the letsencrypt production 
server.

## Admin frontend 
The admin frontend is implemented in the same fashion as the Interchange portal as described above.

## Exporting the database

The chart includes an optional Job that runs `export-application` to snapshot the database to a JSON file on a PersistentVolumeClaim.

Enable it by setting `export.enabled=true` and providing an image in your values override:

```yaml
export:
  enabled: true
  image:
    repository: ghcr.io/nordicwayinterchange/export-application
    tag: "<image tag>"
  persistence:
    enabled: true
    storageClassName: standard
    size: 1Gi
```

The Job is a regular Kubernetes resource (no Helm hooks). It is created when the chart is installed or upgraded with `export.enabled=true` and runs once automatically at that point.

To copy the file out once the Job completes:

```bash
kubectl cp $(kubectl get pod -l component=export -o jsonpath='{.items[0].metadata.name}'):/exports/export.json ./export.json
```

To run the export again, delete the completed Job — it will be recreated on the next deployment.

The exported JSON can be imported into another node using `import-application`.