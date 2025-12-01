import {getTLSAgent} from "@/lib/fetchers/sslAgent";
import axios from "axios";

const headers = {
    Accept: "application/json",
};
const tlsAgent = getTLSAgent();

const fetchIXN: (
    adminUser?: string,
    path?: string,
    selector?: string
) => Promise<any> = async (adminUser, path) => {
    const uri = process.env.INTERCHANGE_URI || "";
    const uriPath = `${adminUser}${path}`;
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
    adminUser: string;
};

export type extendedGetParams = {
    adminUser: string;
    serviceProviderName? : string;
    deliveryId?: string;
    capabilityId?: string;
    shardId?: string;
    pathParam?: string;
};

export type basicGetFunction = (params: basicGetParams) => Promise<any>;
export type extendedGetFunction = (params: extendedGetParams) => Promise<any>;


export const fetchAdminUINeighbours: basicGetFunction = async (params) => {
    const { adminUser} = params;
    return await fetchIXN(adminUser, "/neighbours");
};

export const fetchAdminUIServiceProviders: basicGetFunction = async (params) => {
    const { adminUser} = params;
    return await fetchIXN(adminUser, "/serviceproviders");
};

export const fetchAdminUIPrivateChannels: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/privatechannels`);
};

export const fetchAdminUIPrivateChannelsPeer: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/privatechannels/peer`);
};

export const fetchAdminUIDeliveryIds: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/deliveries`);
};

export const fetchAdminUIDeliveryInfo: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName, deliveryId} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}`);
};

export const fetchAdminUIMatchingCapabilities: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName, deliveryId} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches`);
};

export const fetchAdminUIDeliveryEndpoints: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName, deliveryId} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/endpoints`);
};

export const fetchAdminUIMatchingCapabilityDetails: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName, deliveryId, capabilityId} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches/${capabilityId}`);
};

export const fetchAdminUIMatchingCapabilityShardDetails: extendedGetFunction = async (params) => {
    const { adminUser, serviceProviderName, deliveryId, capabilityId, shardId} = params;
    return await fetchIXN(adminUser, `/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches/${capabilityId}/${shardId}`);
};

export const fetchAdminUIQueueValidator: extendedGetFunction = async (params) => {
    const { adminUser, pathParam } = params;
    return await fetchIXN(adminUser, `/queues/${pathParam}`);
};

export const fetchAdminUIExchangeValidator: extendedGetFunction = async (params) => {
    const { adminUser, pathParam } = params;
    return await fetchIXN(adminUser, `/exchanges/${pathParam}`);
};

export const fetchAdminUIAllExchanges: extendedGetFunction = async (params) => {
    const { adminUser } = params;
    return await fetchIXN(adminUser, `/exchanges`);
};

export const fetchAdminUIAllQueues: extendedGetFunction = async (params) => {
    const { adminUser } = params;
    return await fetchIXN(adminUser, `/queues`);
};

export const fetchAdminUIBiqueueEndpoint: extendedGetFunction = async (params) => {
    const { adminUser } = params;
    return await fetchIXN(adminUser, "/biqueueendpoint");
};