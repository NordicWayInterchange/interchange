import { getTLSAgent } from "@/lib/fetchers/sslAgent";
import { SubscriptionRequest } from "@/types/napcore/subscription";
import axios from "axios";
import { CertificateSignRequest } from "@/types/napcore/certificate";
import { DeliveryRequest } from "@/types/napcore/delivery";
import { CapabilityRequest } from "@/types/napcore/capability";
import { PrivateChannelRequest } from "@/types/napcore/privateChannel";
import { BiQueueEndpointsApi, BiQueueResponse } from "@/types/napcore/biQueueResponse";

const headers = {
  Accept: "application/json",
};
const tlsAgent = getTLSAgent();

const fetchIXN: (
  actorCommonName: string,
  path: string,
  readOnly: boolean,
  selector?: string,
) => Promise<any> = async (actorCommonName, path, readOnly, selector = "" ) => {
  const uri = process.env.INTERCHANGE_URI || "";

  const acn = actorCommonName ? actorCommonName : "";
  const p = path ? path.replace(/^\/|\/$/g, "") : "";

  const uriPath = [acn, p].filter(Boolean).join("/");

  const params: { selector?: string } = {};
  if (selector) {
    params.selector = selector;
  }

  return await axios.get(uri + uriPath, {
    params,
    headers: {
      ...headers,
      "X-Read-Only": String(readOnly),
    },
    httpsAgent: tlsAgent,
  });
};

const postIXN: (
  actorCommonName: string,
  path: string,
  body: SubscriptionRequest | CertificateSignRequest | DeliveryRequest | CapabilityRequest |  {},
  readOnly: boolean
) => Promise<any> = async (actorCommonName, path, body, readOnly) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.post(uri + uriPath, body, {
    headers: {
      ...headers,
      "X-Read-Only": String(readOnly),
    },
    httpsAgent: tlsAgent,
  });
};

const putIXN: (
  actorCommonName: string,
  path: string,
  body: BiQueueResponse | {},
  readOnly: boolean
) => Promise<any> = async (actorCommonName, path, body, readOnly) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.put(uri + uriPath, body, {
    headers: {
      ...headers,
      "X-Read-Only": String(readOnly),
    },
    httpsAgent: tlsAgent,
  });
};

const patchIXN: (
  actorCommonName: string,
  path: string,
  body: PrivateChannelRequest | {},
  readOnly: boolean
) => Promise<any> = async (actorCommonName, path, body, readOnly) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.patch(uri + uriPath, body, {
    headers: {
      ...headers,
      "X-Read-Only": String(readOnly),
    },
    httpsAgent: tlsAgent,
  });
};

const deleteIXN: (
  actorCommonName: string,
  path: string,
  readOnly: boolean
) => Promise<any> = async (actorCommonName, path, readOnly) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.delete(uri + uriPath, {
    headers: {
      ...headers,
      "X-Read-Only": String(readOnly),
    },
    httpsAgent: tlsAgent,
  });
};

// types for handler functions
export type basicGetParams = {
  actorCommonName: string;
  selector?: string;
  readOnly: boolean;
};

export type extendedGetParams = {
  actorCommonName: string;
  pathParam?: string;
  selector?: string;
  readOnly: boolean;
};

export type basicPostParams = {
  actorCommonName: string;
  body?: SubscriptionRequest | CertificateSignRequest | DeliveryRequest | CapabilityRequest | PrivateChannelRequest;
  readOnly: boolean;
};

export type basicPutParams = {
  actorCommonName: string;
  body?: BiQueueResponse | BiQueueEndpointsApi;
  readOnly: boolean;
};

export type basicPatchParams = {
  actorCommonName: string;
  pathParam?:string;
  body?: SubscriptionRequest | CertificateSignRequest | DeliveryRequest | CapabilityRequest | PrivateChannelRequest;
  readOnly: boolean;
};

export type basicDeleteParams = {
  actorCommonName: string;
  pathParam?:string;
  firstParam?: string;
  secondParam?: string;
  readOnly: boolean;
};

export type basicGetFunction = (params: basicGetParams) => Promise<any>;
export type extendedGetFunction = (params: extendedGetParams) => Promise<any>;
export type basicPostFunction = (params: basicPostParams) => Promise<any>;
export type basicPutFunction = (params: basicPutParams) => Promise<any>;
export type basicPatchFunction = (params: basicPatchParams) => Promise<any>;
export type basicDeleteFunction = (params: basicDeleteParams) => Promise<any>;

export const fetchNapcoreNetworkCapabilities: basicGetFunction = async (
  params
) => {
  const { actorCommonName, selector = "", readOnly } = params;
  return await fetchIXN(actorCommonName, "/subscriptions/capabilities", readOnly, selector);
};

export const fetchNapcoreSubscriptions: extendedGetFunction = async (
  params
) => {
  const { actorCommonName, selector = "", readOnly } = params;
  return await fetchIXN(actorCommonName, `/subscriptions`, readOnly, selector);
};

export const addNapcoreSubscriptions: basicPostFunction = async (params) => {
  const { actorCommonName, body = {}, readOnly } = params;
  return await postIXN(actorCommonName, "/subscriptions", body, readOnly);
};

export const deleteNapcoreSubscriptions: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam, readOnly} = params;
  return await deleteIXN(actorCommonName, `/subscriptions/${pathParam}`, readOnly);
};

export const addNapcoreCertificates: basicPostFunction = async (params) => {
  const { actorCommonName, body = {}, readOnly } = params;
  return await postIXN(actorCommonName, "/x509/csr", body, readOnly);
};

export const fetchNapcoreDeliveries: basicGetFunction = async (params: {
  actorCommonName: string;
  readOnly: boolean;
  selector?: string;
}) => {
  const { actorCommonName, selector = "", readOnly} = params;
  return await fetchIXN(actorCommonName, "/deliveries", readOnly, selector);
};

export const fetchNapcoreDeliveriesCapabilities: basicGetFunction = async (
  params
) => {
  const { actorCommonName, readOnly, selector = "" } = params;
  return await fetchIXN(actorCommonName, "/deliveries/capabilities", readOnly, selector);
};

export const deleteNapcoreDeliveries: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam, readOnly } = params;
  return await deleteIXN(actorCommonName, `/deliveries/${pathParam}`, readOnly);
};

export const addNapcoreDeliveries: basicPostFunction = async (params) => {
  const { actorCommonName, body = {}, readOnly } = params;
  return await postIXN(actorCommonName, "/deliveries", body, readOnly);
};

export const fetchNapcoreCapabilities: basicGetFunction = async (params) => {
  const { actorCommonName, readOnly, selector = "" } = params;
  return await fetchIXN(actorCommonName, "/capabilities", readOnly, selector);
};

export const fetchNapcorePublicationIds: basicGetFunction = async (params) => {
  const { actorCommonName, readOnly, selector = "" } = params;
  return await fetchIXN(actorCommonName,
    "/capabilities/publicationids",
    readOnly,
    selector);
};

export const addNapcoreCapabilities: basicPostFunction = async (params) => {
  const { actorCommonName, body = {}, readOnly } = params;
  return await postIXN(actorCommonName, "/capabilities", body, readOnly);
};

export const deleteNapcoreCapabilities: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam, readOnly } = params;
  return await deleteIXN(actorCommonName, `/capabilities/${pathParam}`, readOnly);
};

export const fetchNapcorePrivateChannels: extendedGetFunction = async (
  params
) => {
  const { actorCommonName, readOnly } = params;
  return await fetchIXN(actorCommonName, `/privatechannels`, readOnly);
};

export const addNapcorePrivateChannels: basicPostFunction = async (params) => {
  const { actorCommonName, body = {}, readOnly } = params;
  return await postIXN(actorCommonName, "/privatechannels", body, readOnly);
};

export const addNapcorePeerToExistingPrivateChannel: basicPatchFunction = async (params) => {
  const { actorCommonName, pathParam, body = {}, readOnly } = params;
  return await patchIXN(actorCommonName, `/privatechannels/peer/${pathParam}`, body, readOnly);
};

export const deleteNapcorePrivateChannels: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam, readOnly } = params;
  return await deleteIXN(actorCommonName, `/privatechannels/${pathParam}`, readOnly);
};

export const deleteNapcoreMyselfFromSubscribedPrivateChannel: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam, readOnly } = params;
  return await deleteIXN(actorCommonName, `/privatechannels/peer/${pathParam}`, readOnly);
};

export const deleteNapcorePeerFromExistingPrivateChannel: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, firstParam, secondParam, readOnly } = params;
  return await deleteIXN(actorCommonName, `/privatechannels/peer/${firstParam}/${secondParam}`, readOnly);
};

export const fetchNapcorePrivateChannelsPeers: extendedGetFunction = async (
  params
) => {
  const { actorCommonName, readOnly } = params;
  return await fetchIXN(actorCommonName, `/privatechannels/peer`, readOnly);
};

export const fetchNapcoreAccessToBiQueue: extendedGetFunction = async (
  params
) => {
  const { actorCommonName, readOnly } = params;
  return await fetchIXN(actorCommonName, `/biconsumer`, readOnly);
};

export const fetchNapcoreBiQueueEndpoints: extendedGetFunction = async (params) => {
  const { readOnly } = params;
  return await fetchIXN("", "/biqueueendpoints", readOnly);
};

export const addNapcoreAccessToBiQueue: basicPutFunction = async (params) => {
  const { actorCommonName, body = {}, readOnly } = params;
  return await putIXN(actorCommonName, "/biconsumer", body, readOnly);
};
