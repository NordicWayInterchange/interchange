import logger from "@/lib/logger";
import {NextApiRequest, NextApiResponse} from "next";
import { getServerSession } from 'next-auth/next';
import {getToken} from "next-auth/jwt";
import {
    fetchAdminUINeighbours,
    fetchAdminUIQueueValidator,
    fetchAdminUIServiceProviders
} from "@/lib/fetchers/interchangeConnector";
import {Neighbours} from "@/types/neighbours";
import { authOptions } from "@/pages/api/auth/[...nextauth]";
import {Session} from "next-auth";

interface CustomSession extends Session {
    user: {
        commonName: string;
        email?: string;
    };
}

const fetchNeighbours = async (params: basicGetParams) => {
    const res = await fetchAdminUINeighbours(params);
    const neighbours: Array<Neighbours> = await res.data;
    return [res.status, neighbours];
};

const fetchServiceProviders = async (params: basicGetParams) => {
    const res = await fetchAdminUIServiceProviders(params);
    const serviceProviders: Array<Neighbours> = await res.data;
    return [res.status, serviceProviders];
};

const fetchQueueValidator = async (params: extendedGetParams) => {
    const res = await fetchAdminUIQueueValidator(params);
    const queueExists: boolean = await res.data;
    return [res.status, queueExists];
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
export type extendedGetFunction = (params: extendedGetParams) => Promise<any>;

const getPaths: {
    [key: string]: basicGetFunction | extendedGetFunction;
} = {
    "neighbours": fetchNeighbours,
    "serviceproviders": fetchServiceProviders,
    "queueValidator": fetchQueueValidator,
};
const findHandler: (params: any) =>
    | {
    fn:
        | basicGetFunction
        | extendedGetFunction;
    params:
        | basicGetParams
        | extendedGetParams;
}
    | {} = (params) => {
    const {
        path = [],
        method,
        actorCommonName,
        selector = "",
    } = params;
    switch (method) {
        case "GET":
            const possiblePaths = Object.keys(getPaths);
            const lastSegment = path[path.length - 1];
            const fn = getPaths[lastSegment];
            if (possiblePaths.includes(lastSegment)) {
                return {
                    fn,
                    params: { actorCommonName, selector },
                };
            }

        default:
            return {};
    }
};

const isAuthenticated = async (req: NextApiRequest, res: NextApiResponse) => {
    const secret = process.env.NEXTAUTH_SECRET;
    const token = await getToken({ req, secret, raw: true });
    const session = await getServerSession(req as any, res as any, authOptions as any);

    const typedSession = session as CustomSession;


    return !(
        !token ||
        !session ||
        !req.query.slug ||
        !typedSession.user ||
        typedSession.user.commonName !== req.query.slug[0]
    );
};

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    const session = await getServerSession(req as any, res as any, authOptions as any);
    const typedSession = session as CustomSession;

    if (!typedSession?.user?.email) {
        logger.info("Access denied - No session or user email not available.");
        return res.status(401).json({ message: 'Unauthorized' });
    }

    if (!(await isAuthenticated(req, res))) {
        logger.info("Access denied - User with email: " +
            typedSession.user.email +
            ", doesn't have permission to perform this action");

        return res.status(403).json({ description: `Access denied - You don't have permission` });
    }

    const slug = Array.isArray(req.query.slug)
        ? req.query.slug
        : [req.query.slug];

    const selector = Array.isArray(req.query.selector)
        ? req.query.selector[0]
        : req.query.selector;

    const [actorCommonName, ...path] = slug;
    const urlPath = path.join("/");
    const { method, body } = req;

    if (actorCommonName && path) {
        const executer = findHandler({
            method,
            path,
            body,
            actorCommonName,
            selector,
        });

        if (executer && "fn" in executer) {
            try {
                const { fn, params } = executer;
                const [status, data] = await fn(params);

                logger
                    .child({
                        params,
                        method: req.method,
                        httpStatus: status,
                        url: req.url,
                        user: typedSession.user,
                        slug: req.query.slug,
                    })
                    .info({params: params, method: method, httpsStatus: status, url: req.url, user: typedSession.user, slug: req.query.slug});

                return res.status(status).json(data);
            } catch (error: any) {
                if(error.response) {
                    logger.error({
                        errorStatus: error.response.status,
                        errorData: error.response.data,
                    });
                    return res.status(error.response.status).json(error.response.data);
                } else {
                    logger.error(error.message);
                    return res.status(500).json("Error fetching from server");
                }
            }
        }
    }

    logger.info("Page not found: " + urlPath);
    return res.status(404).json({ description: `Page not found: ${urlPath}` });
}
