import {getTLSAgent} from "@/lib/fetchers/sslAgent";
import axios from "axios";

const headers = {
    Accept: "application/json",
};
const tlsAgent = getTLSAgent();

const fetchIXN: (
    actorCommonName: string,
    path: string,
    selector?: string
) => Promise<any> = async (actorCommonName, path) => {
    const uri = process.env.INTERCHANGE_URI || "";
    const uriPath = `${actorCommonName}${path}`;
    const params: { selector?: string } = {};
    try {
    return await axios.get(uri + uriPath, {
        params,
        headers,
        httpsAgent: tlsAgent,
    });
} catch (error: any) {
    if (error.response) {
        console.error("Server responded with an error:", error.response.data);
        return { error: "Server Error", statusCode: error.response.status, message: error.response.data };
    } else if (error.request) {
        console.error("No response received from server:", error.request);
        return { error: "No Response", message: "No response from server", request: error.request };
    } else {
        console.error("Error setting up the request:", error.message);
        return { error: "Request Setup Error", message: error.message };
    }
}
};

export type basicGetParams = {
    actorCommonName: string;
};

export type extendedGetParams = {
    actorCommonName: string;
    serviceProviderName? : string;
    deliveryId?: string;
    capabilityId?: string;
    shardId?: string;
    pathParam?: string;
};

export type basicGetFunction = (params: basicGetParams) => Promise<any>;
export type extendedGetFunction = (params: extendedGetParams) => Promise<any>;


export const fetchAdminUINeighbours: basicGetFunction = async (params) => {
    const { actorCommonName} = params;
    return await fetchIXN(actorCommonName, "/neighbours");
};

export const fetchAdminUIServiceProviders: basicGetFunction = async (params) => {
    const { actorCommonName} = params;
    return await fetchIXN(actorCommonName, "/serviceproviders");
};

export const fetchAdminUIPrivateChannels: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/privatechannels`);
};

export const fetchAdminUIDeliveryIds: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/deliveries`);
};

export const fetchAdminUIDeliveryInfo: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName, deliveryId} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}`);
};

export const fetchAdminUIMatchingCapabilities: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName, deliveryId} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches`);
};

export const fetchAdminUIDeliveryEndpoints: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName, deliveryId} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/endpoints`);
};

export const fetchAdminUIMatchingCapabilityDetails: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName, deliveryId, capabilityId} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches/${capabilityId}`);
};

export const fetchAdminUIMatchingCapabilityShardDetails: extendedGetFunction = async (params) => {
    const { actorCommonName, serviceProviderName, deliveryId, capabilityId, shardId} = params;
    return await fetchIXN(actorCommonName, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches/${capabilityId}/${shardId}`);
};

export const fetchAdminUIQueueValidator: extendedGetFunction = async (params) => {
    const { actorCommonName, pathParam } = params;
    return await fetchIXN(actorCommonName, `/queues/${pathParam}`);
};

export const fetchAdminUIExchangeValidator: extendedGetFunction = async (params) => {
    const { actorCommonName, pathParam } = params;
    return await fetchIXN(actorCommonName, `/exchanges/${pathParam}`);
};

export const fetchAdminUIAllExchanges: extendedGetFunction = async (params) => {
    const { actorCommonName } = params;
    return await fetchIXN(actorCommonName, `/exchanges`);
};