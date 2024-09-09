# Interchange Helm charts

The helm charts in this folder is uploaded to the registry at oci://europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/helm/interchange
The current version can be located by looking at the version listed in the chart file [here](https://raw.githubusercontent.com/NordicWayInterchange/interchange/federation-master/helm/interchange/Chart.yaml)

## Some important values

### Portal
The Interchange includes a simple portal implementation that uses Auth0 for authentication and letsencrypt for 
certificate management, as well as nginx-ingress as ingress. These components require the ability to install
components in separate namespaces. Additional requirements are a separate static IP for the portal, and an DNS A-record
for the domain name the portal is to be served from.

To enable the portal, first install [Niginx ingress](https://kubernetes.github.io/ingress-nginx/) and [cert-manager](https://cert-manager.io/)
into their separate namespaces. 
The nginx ingress might need a separate LoadBalancer in front, in that case, the setting `controller.extraArgs` need to contain the setting 
`publish-service: ingress-nginx/ingress-lb` (TODO: this should have the namespace in the lb service name)
Then, in order to install the portal, review the example values in the [example file](example_with_napcore.yml), and set the 
value of `napcore_frontend.enabled` to true.
It is advised to set the `letsencrypt.server` variable to the letsencrypt staging server until you are completely convinced
that the setup is successful, so you don't wind up hitting the strict limits of the letsencrypt production server.
