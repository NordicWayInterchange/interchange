import { getTLSAgent } from "@/lib/fetchers/sslAgent";
import { SubscriptionRequest } from "@/types/napcore/subscription";
import axios from "axios";
import { CertificateSignRequest } from "@/types/napcore/certificate";
import { DeliveryRequest } from "@/types/napcore/delivery";
import { CapabilityRequest } from "@/types/napcore/capability";
import { PrivateChannelRequest } from "@/types/napcore/privateChannel";
import { BiQueueEndpointResponse, BiQueueResponse } from "@/types/napcore/biQueueResponse";

const headers = {
  Accept: "application/json",
};
const tlsAgent = getTLSAgent();

const fetchIXN: (
  actorCommonName: string,
  path: string,
  selector?: string
) => Promise<any> = async (actorCommonName, path, selector = "") => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  const params: { selector?: string } = {};
  if (selector) {
    params.selector = selector;
  }

  return await axios.get(uri + uriPath, {
    params,
    headers,
    httpsAgent: tlsAgent,
  });
};

const postIXN: (
  actorCommonName: string,
  path: string,
  body: SubscriptionRequest | CertificateSignRequest | DeliveryRequest | CapabilityRequest |  {}
) => Promise<any> = async (actorCommonName, path, body) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.post(uri + uriPath, body, {
    headers,
    httpsAgent: tlsAgent,
  });
};

const putIXN: (
  actorCommonName: string,
  path: string,
  body: BiQueueResponse | {}
) => Promise<any> = async (actorCommonName, path, body) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.put(uri + uriPath, body, {
    headers,
    httpsAgent: tlsAgent,
  });
};

const patchIXN: (
  actorCommonName: string,
  path: string,
  body: PrivateChannelRequest | {}
) => Promise<any> = async (actorCommonName, path, body) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.patch(uri + uriPath, body, {
    headers,
    httpsAgent: tlsAgent,
  });
};

const deleteIXN: (
  actorCommonName: string,
  path: string
) => Promise<any> = async (actorCommonName, path) => {
  const uri = process.env.INTERCHANGE_URI || "";
  const uriPath = `${actorCommonName}${path}`;
  return await axios.delete(uri + uriPath, {
    headers,
    httpsAgent: tlsAgent,
  });
};

// types for handler functions
export type basicGetParams = {
  actorCommonName: string;
  selector?: string;
};

export type extendedGetParams = {
  actorCommonName: string;
  pathParam?: string;
  selector?: string;
};

export type basicPostParams = {
  actorCommonName: string;
  body?: SubscriptionRequest | CertificateSignRequest | DeliveryRequest | CapabilityRequest | PrivateChannelRequest;
};

export type basicPutParams = {
  actorCommonName: string;
  body?: BiQueueResponse | BiQueueEndpointResponse;
};

export type basicPatchParams = {
  actorCommonName: string;
  pathParam?:string;
  body?: SubscriptionRequest | CertificateSignRequest | DeliveryRequest | CapabilityRequest | PrivateChannelRequest;
};

export type basicDeleteParams = {
  actorCommonName: string;
  pathParam?:string;
  firstParam?: string;
  secondParam?: string;
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
  const { actorCommonName, selector = "" } = params;
  return await fetchIXN(actorCommonName, "/subscriptions/capabilities", selector
  );
};

export const fetchNapcoreSubscriptions: extendedGetFunction = async (
  params
) => {
  const { actorCommonName, selector = "" } = params;
  return await fetchIXN(actorCommonName, `/subscriptions`, selector);
};

export const addNapcoreSubscriptions: basicPostFunction = async (params) => {
  const { actorCommonName, body = {} } = params;
  return await postIXN(actorCommonName, "/subscriptions", body);
};

export const deleteNapcoreSubscriptions: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam } = params;
  return await deleteIXN(actorCommonName, `/subscriptions/${pathParam}`);
};

export const addNapcoreCertificates: basicPostFunction = async (params) => {
  const { actorCommonName, body = {} } = params;
  return await postIXN(actorCommonName, "/x509/csr", body);
};

export const fetchNapcoreDeliveries: basicGetFunction = async (params: {
  actorCommonName: string;
  selector?: string;
}) => {
  const { actorCommonName, selector = "" } = params;
  return await fetchIXN(actorCommonName, "/deliveries", selector);
};

export const fetchNapcoreDeliveriesCapabilities: basicGetFunction = async (
  params
) => {
  const { actorCommonName, selector = "" } = params;
  return await fetchIXN(actorCommonName, "/deliveries/capabilities", selector);
};

export const deleteNapcoreDeliveries: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam } = params;
  return await deleteIXN(actorCommonName, `/deliveries/${pathParam}`);
};

export const addNapcoreDeliveries: basicPostFunction = async (params) => {
  const { actorCommonName, body = {} } = params;
  return await postIXN(actorCommonName, "/deliveries", body);
};

export const fetchNapcoreCapabilities: basicGetFunction = async (params) => {
  const { actorCommonName, selector = "" } = params;
  return await fetchIXN(actorCommonName, "/capabilities", selector);
};

export const fetchNapcorePublicationIds: basicGetFunction = async (params) => {
  const { actorCommonName, selector = "" } = params;
  return await fetchIXN(actorCommonName,
    "/capabilities/publicationids",
    selector);
};

export const addNapcoreCapabilities: basicPostFunction = async (params) => {
  const { actorCommonName, body = {} } = params;
  return await postIXN(actorCommonName, "/capabilities", body);
};

export const deleteNapcoreCapabilities: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam } = params;
  return await deleteIXN(actorCommonName, `/capabilities/${pathParam}`);
};

export const fetchNapcorePrivateChannels: extendedGetFunction = async (
  params
) => {
  const { actorCommonName } = params;
  return await fetchIXN(actorCommonName, `/privatechannels`);
};

export const addNapcorePrivateChannels: basicPostFunction = async (params) => {
  const { actorCommonName, body = {} } = params;
  return await postIXN(actorCommonName, "/privatechannels", body);
};

export const addNapcorePeerToExistingPrivateChannel: basicPatchFunction = async (params) => {
  const { actorCommonName, pathParam, body = {} } = params;
  return await patchIXN(actorCommonName, `/privatechannels/peer/${pathParam}`, body);
};

export const deleteNapcorePrivateChannels: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam } = params;
  return await deleteIXN(actorCommonName, `/privatechannels/${pathParam}`);
};

export const deleteNapcoreMyselfFromSubscribedPrivateChannel: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, pathParam } = params;
  return await deleteIXN(actorCommonName, `/privatechannels/peer/${pathParam}`);
};

export const deleteNapcorePeerFromExistingPrivateChannel: basicDeleteFunction = async (
  params
) => {
  const { actorCommonName, firstParam, secondParam } = params;
  return await deleteIXN(actorCommonName, `/privatechannels/peer/${firstParam}/${secondParam}`);
};

export const fetchNapcorePrivateChannelsPeers: extendedGetFunction = async (
  params
) => {
  const { actorCommonName } = params;
  return await fetchIXN(actorCommonName, `/privatechannels/peer`);
};

export const fetchNapcoreAccessToBiQueue: extendedGetFunction = async (
  params
) => {
  const { actorCommonName } = params;
  return await fetchIXN(actorCommonName, `/biconsumer`);
};

export const fetchNapcoreBiQueueEndpoint: extendedGetFunction = async (
  params
) => {
  const { actorCommonName } = params;
  return await fetchIXN(actorCommonName, `/biqueueEndpoint`);
};

export const addNapcoreAccessToBiQueue: basicPutFunction = async (params) => {
  const { actorCommonName, body = {} } = params;
  return await putIXN(actorCommonName, "/biconsumer", body);
};
