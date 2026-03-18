export const createSubscription = (actorCommonName: string, body: Object) => {
  return fetch(`/api/${actorCommonName}/subscriptions`, {
    method: "post",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
};

export const deleteSubscriptions = (
  actorCommonName: string,
  subscriptionId: string
) => {
  return fetch(`/api/${actorCommonName}/subscriptions/${subscriptionId}`, {
    method: "delete",
  });
};

export const createCertificate = (actorCommonName: string, csr: string) => {
  const csrRequest = {
    csr: csr,
  };
  return fetch(`/api/${actorCommonName}/x509/csr`, {
    method: "post",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(csrRequest),
  });
};

export const createDelivery = (actorCommonName: string, body: Object) => {
  return fetch(`/api/${actorCommonName}/deliveries`, {
    method: "post",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
};

export const deleteDeliveries = (
  actorCommonName: string,
  deliveryId: string
) => {
  return fetch(`/api/${actorCommonName}/deliveries/${deliveryId}`, {
    method: "delete",
  });
};

export const createPrivateChannel = (actorCommonName: string, body: Object) => {
  return fetch(`/api/${actorCommonName}/privatechannels`, {
    method: "post",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
};

export const addPeerToExistingPrivateChannel = (actorCommonName: string, privateChannelId: string, body: Object) => {
  return fetch(`/api/${actorCommonName}/privatechannels/peer/${privateChannelId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
};

export const deletePrivateChannel = (
  actorCommonName: string,
  privateChannelId: string
) => {
  return fetch(`/api/${actorCommonName}/privatechannels/${privateChannelId}`, {
    method: "delete",
  });
};

export const deletePeerFromExistingPrivateChannel = (
  actorCommonName: string,
  privateChannelId: string,
  peerName: string,
) => {
  return fetch(`/api/${actorCommonName}/privatechannels/peer/${privateChannelId}/${peerName}`, {
    method: "delete",
  });
};

export const deleteMyselfFromSubscribedPrivateChannel = (
  actorCommonName: string,
  privateChannelId: string
) => {
  return fetch(`/api/${actorCommonName}/privatechannels/peer/${privateChannelId}`, {
    method: "delete",
  });
};

export const createUserCapability = (actorCommonName: string, body: Object) => {
  return fetch(`/api/${actorCommonName}/capabilities`, {
    method: "post",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
};

export const deleteUserCapability = (
  actorCommonName: string,
  capabilityId: string
) => {
  return fetch(`/api/${actorCommonName}/capabilities/${capabilityId}`, {
    method: "delete",
  });
};

export const addBiqueueAccess = (actorCommonName: string, body: Object) => {
  return fetch(`/api/${actorCommonName}/biconsumer`, {
    method: "put",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
};

