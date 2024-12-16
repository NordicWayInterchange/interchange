import logger from "@/lib/logger";
import {NextApiRequest, NextApiResponse} from "next";
import {getServerSession} from "next-auth/next";
import {authOptions} from "@/pages/api/auth/[...nextauth]";
import {getToken} from "next-auth/jwt";
import {fetchAdminUINeighbours} from "@/lib/fetchers/interchangeConnector";
import {Neighbours} from "@/types/neighbours";


/*function extractCauseCodes(neighbours: Neighbours) {
    let causeCodes;
    if (
        "causeCode" in capability.application &&
        capability.application.causeCode
    ) {
        causeCodes = capability.application.causeCode.map((causeCode) => {
            return causeCodesList.find((c) => c.value === causeCode) || { "value": causeCode};
        });
    }
    return causeCodes;
}*/

const fetchNeighbours = async (params: basicGetParams) => {
    console.log('MARYAM');
    debugger
    const res = await fetchAdminUINeighbours(params);
    const neigbours: Array<Neighbours> = await res.data;
    console.log('NONONO', neigbours)
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
    const urlPath = path.join("/");
    console.log('urlPath', urlPath)
    switch (method) {
        case "GET":
            debugger
            const possiblePaths = Object.keys(getPaths);
            if (possiblePaths.includes(urlPath)) {
                return {
                    fn: getPaths[urlPath],
                    params: { actorCommonName, selector },
                };
            }
            if (path.length > 1 && possiblePaths.includes(path[0])) {
                return {
                    fn: getPaths[path[0]],
                    params: { actorCommonName, pathParam: path[1] },
                };
            }

        default:
            return {};
    }
};

const isAuthenticated = async (req: NextApiRequest, res: NextApiResponse) => {
    const secret = process.env.NEXTAUTH_SECRET;
    const token = await getToken({ req, secret, raw: true });
    const session = await getServerSession(req, res, authOptions);

    return !(
        !token ||
        !session ||
        !req.query.slug ||
        !session.user ||
        session.user.commonName !== req.query.slug[0]
    );
};

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    console.log('MARYM')
    const session = await getServerSession(req, res, authOptions);

    if (!(await isAuthenticated(req, res))) {
        logger.info(
            "Access denied - " +
            "User with email: " +
            session.user.email +
            ", don't have permission to perform this action"
        );

        return res
            .status(403)
            .json({ description: `Access denied - You don't have permission` });
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
                        user: session.user,
                        slug: req.query.slug,
                    })
                    .info();

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
