# Interchange Helm charts

The helm charts in this folder is uploaded to the registry at oci://ghcr.io/nordicwayinterchange/interchange
The current version can be located by looking at the version listed in the chart file [here](https://raw.githubusercontent.com/NordicWayInterchange/interchange/federation-master/helm/interchange/Chart.yaml)

# Enabling and disabling resources

For testing, the different components of the interchange can be enabled or disabled using switches called "enabled". 
These are by default set to ```true```, except for the ones controlling the portal and admin frontend, which are set to 
```false``` by default.
In addition, the broker template has a switch to turn on and off the external service. This can be used if a new broker
version is installed, and one needs to populate the broker from the database before letting producers and consumers in.

# Exposing

If you want to expose the application to the world, the IP's/ports for onboard-server, neighbour-server and qpid-secure
should be exposed. How this is done depends on the deployment/cloud used.

## Internal Qpid TLS

Qpid's HTTPS management endpoint and routing-configurer can use separate TLS
material from the node-wide TLS Secret. Qpid can mount an externally managed
internal Secret, while routing-configurer has a separate opt-in TLS switch.
Each side can be enabled independently for migration and testing:

```yaml
qpid:
  internal_secrets_name: qpid-internal-tls
  internal_key_store_name: qpid-server.p12
  internal_secret:
    externally_managed: true
    trust_store_name: routing-configurer-ca.jks

routing_configurer:
  internal_tls:
    enabled: true
    secret_name: routing-configurer-internal-tls
```

When `qpid.internal_secret.externally_managed` is true,
`qpid.internal_secrets_name` references an externally managed Kubernetes
Secret. It must contain `qpid.internal_key_store_name`,
`qpid.internal_secret.trust_store_name`, and the configured password keys.
When false, the chart creates that Secret from the legacy `internal_keystore`
value. The routing-configurer Secret must contain its configured keystore,
truststore, and password keys. Default key names are documented in
`values.yaml`.

The Qpid internal truststore must trust the routing-configurer client
certificate. The routing-configurer internal truststore must trust the Qpid
server certificate. Qpid's internal server certificate must include
`<release-name>-qpid-http` as a DNS subject alternative name because that is
the hostname used by routing-configurer.

With either switch disabled, that component keeps its existing TLS setup. This
makes it possible to migrate one side at a time, provided the enabled side
trusts the certificate still used by the other side.

### Qpid secrets through the Secrets Store CSI driver

Qpid can mount both its node TLS files and internal TLS files from existing
`SecretProviderClass` resources. It can also read its administrator and TLS
passwords from Kubernetes Secrets synchronized by the CSI driver. All settings
are opt-in: the defaults continue to use Kubernetes Secret volumes and the
top-level Helm password values.

```yaml
qpid:
  serviceAccount:
    create: true
    name: interchange-qpid
    annotations:
      azure.workload.identity/client-id: "<managed-identity-client-id>"

  podLabels:
    azure.workload.identity/use: "true"

  tls:
    source: csi
    csi:
      secret_provider_class: interchange-qpid-node-tls

  credentials:
    source: secret
    secret_name: interchange-qpid-credentials

  internal_secrets_name: interchange-qpid-internal-credentials
  internal_secret:
    externally_managed: true
    source: csi
    csi:
      secret_provider_class: interchange-qpid-internal-tls
```

The chart references existing `SecretProviderClass` resources in the release
namespace; it does not create provider-specific resources. The node TLS class
must mount files named according to `self.key_store` and `self.trust_store`.
The internal TLS class must mount files named according to
`qpid.internal_key_store_name` and `qpid.internal_secret.trust_store_name`.

When `qpid.credentials.source` is `secret`, the Secret configured by
`qpid.credentials.secret_name` must contain `admin_password`,
`key_store_password`, and `trust_store_password` by default. The key names can
be changed under `qpid.credentials`. In this mode the Qpid ConfigMap references
`ADMIN_PASSWORD` instead of containing the administrator password.

When `qpid.internal_secret.externally_managed` is true, the Secret configured
by `qpid.internal_secrets_name` must contain the internal keystore and
truststore password keys. These Secrets can be populated with `secretObjects`
in either of the mounted `SecretProviderClass` resources. A synchronized Secret
exists only while at least one pod mounts its associated class.

For Azure Workload Identity, the AKS cluster must have its OIDC issuer and
Workload Identity enabled. The federated identity credential subject must
match the release namespace and configured ServiceAccount name:

```text
system:serviceaccount:<namespace>:interchange-qpid
```

The CSI driver, Azure provider, managed identity, Key Vault permissions, and
referenced `SecretProviderClass` resources must be installed separately. See
`example_with_azure_qpid_csi.yml` for a complete chart override and
`CHART_TESTS.md` for rendering and cluster test steps.

## Database password

The PostgreSQL container can receive its password from an environment value or
a Secrets Store CSI volume. Environment delivery is the default and continues
to use the top-level `postgres_password` value:

```yaml
postgres_password: federation
```

To mount the password through the Secrets Store CSI driver, reference an
existing `SecretProviderClass` in the release namespace:

```yaml
database:
  password:
    source: csi
    mount_path: /run/secrets/database
    csi:
      secret_provider_class: interchange-database-password
```

The CSI driver and its provider must already be installed. The referenced
`SecretProviderClass` must expose the password as a file named `password`.
CSI mode sets PostgreSQL's `POSTGRES_PASSWORD_FILE` to `<mount_path>/password`.

This setting applies only to the PostgreSQL container. The Interchange
applications continue to receive their database password from the top-level
`postgres_password` value.

### Azure Workload Identity for the database

The database pod can use a dedicated Kubernetes ServiceAccount for access to
Azure Key Vault through the Secrets Store CSI driver. This is opt-in; by
default, the chart does not create or assign a ServiceAccount, preserving the
existing behavior on Google Cloud and other Kubernetes installations.

```yaml
database:
  password:
    source: csi
    mount_path: /run/secrets/database
    csi:
      secret_provider_class: interchange-database-password

  serviceAccount:
    create: true
    name: interchange-database
    annotations:
      azure.workload.identity/client-id: "<managed-identity-client-id>"

  podLabels:
    azure.workload.identity/use: "true"
```

When `database.serviceAccount.create` is true, an explicit
`database.serviceAccount.name` is required because the Azure federated identity
credential is bound to that name. Set `create` to false and provide `name` to
use an existing ServiceAccount. Leave both at their defaults to use the
namespace's default ServiceAccount.

The AKS cluster must have its OIDC issuer and Workload Identity enabled. The
federated identity credential subject must match the release namespace and
configured ServiceAccount name:

```text
system:serviceaccount:<namespace>:interchange-database
```

The CSI driver, Azure provider, and referenced `SecretProviderClass` must be
installed separately. The managed identity also needs permission to read the
configured Key Vault secret.


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
