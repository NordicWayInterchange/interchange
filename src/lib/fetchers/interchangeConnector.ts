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

export type basicGetParams = {
    actorCommonName: string;
    selector?: string;
};
export type extendedGetParams = {
    actorCommonName: string;
    pathParam?: string;
    selector?: string;
};

export type basicGetFunction = (params: basicGetParams) => Promise<any>;


export const fetchAdminUINeighbours: basicGetFunction = async (params) => {
    const { actorCommonName} = params;
    return await fetchIXN(actorCommonName, "/neighbours");
};