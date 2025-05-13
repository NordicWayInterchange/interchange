import React, {useEffect, useRef, useState} from 'react';
import * as d3 from 'd3';
import {Box} from "@mui/system";

import {useSession} from "next-auth/react";
import {useFetchMatchingCapability} from "@/hooks/useFetchMatchingCapability";
import {Card, CardContent, Collapse, IconButton, List, Typography} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Loading from "@/components/shared/components/Loading";
import {GraphSection} from "@/pages/GraphSection";

const BindingExists: React.FC = () => {
    const {data: session} = useSession();
    const {data: matchingCapabilities} = useFetchMatchingCapability(
        session?.user.commonName as string
    );
    const [expanded, setExpanded] = useState<boolean>(true);

    const handleExpandClick = () => {
        setExpanded((prev) => !prev);
    };

    const data = {
        "deliveryId": "5090213f-9c2f-40a0-972c-4a730a5c0317",
        "capabilityMatchApi": [
            {
                "capabilityId": "e49df956-bfb9-4849-bc07-903f30e9c4ec",
                "shardId": 2,
                "binding": {
                    "bindingKey": "del-1b39c7b9-f27d-4149-9422-54360333be33",
                    "destination": "cap-53bf21ce-0034-46c8-a0e5-60ad5716b6ba",
                    "arguments": {
                        "x-filter-jms-selector": "((quadTree like '%,1203%') AND (causeCode = 5) AND (messageType = 'DENM') AND (publicationId = 'NO00002:testqaw') AND (publisherId = 'NO00002') AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')) AND (originatingCountry = 'NO')"
                    }
                },
                "exists": true
            }
        ]
    }
    const svgRef = useRef<SVGSVGElement | null>(null);

    const [copyTargets, setCopyTargets] = useState<
        { id: string; fullId: string; x: number; y: number }[]
    >([]);


    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll('*').remove(); // Clear previous render

        const width = 1000;
        const rectWidth = 120;
        const rectHeight = 50;
        const verticalSpacing = 150;
        const horizontalSpacing = 200;

        const trimId = (id: string) => {
            const parts = id.split("-");
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: { id: string; fullId: string; x: number; y: number }[] = [];

        const addCopyTarget = (id: string, fullId: string, x: number, y: number) => {
            targets.push({ id, fullId, x, y });
        };

        let yOffset = 50;

        matchingCapabilities?.forEach((entry, providerIndex) => {
            const { serviceProviderName, matches } = entry;

            matches.forEach((match, matchIndex) => {
                const deliveryIdX = width / 2 - rectWidth / 2;
                const deliveryIdY = yOffset;

                svg.append('text')
                    .attr('x', deliveryIdX + rectWidth / 2)
                    .attr('y', deliveryIdY - 10)
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#555')
                    .attr('font-size', 12)
                    .text('DeliveryId');

                svg.append('rect')
                    .attr('x', deliveryIdX)
                    .attr('y', deliveryIdY)
                    .attr('width', rectWidth)
                    .attr('height', rectHeight)
                    .attr('fill', '#88c');

                svg.append('text')
                    .attr('x', deliveryIdX + rectWidth / 2)
                    .attr('y', deliveryIdY + 30)
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#000')
                    .attr('font-size', 14)
                    .text(trimId(match.deliveryId));

                addCopyTarget(`deliveryId-${serviceProviderName}-${matchIndex}`, match.deliveryId, deliveryIdX + rectWidth / 2, deliveryIdY + rectHeight / 2);

                const capabilityMatchList = match.capabilityMatchApi.filter((c: { exists: any; }) => c.exists);
                const isSingle = capabilityMatchList.length === 1;
                const totalWidth = horizontalSpacing * (capabilityMatchList.length - 1);
                const startX = isSingle ? width / 2 - rectWidth / 2 : width / 2 - totalWidth / 2;

                capabilityMatchList.forEach((capability: { binding: { bindingKey: string; }; capabilityId: string; }, i: number) => {
                    const capabilityIdX = startX + i * horizontalSpacing;
                    const bindingY = deliveryIdY + rectHeight + 40;
                    const capabilityIdY = bindingY + verticalSpacing;

                    svg.append('line')
                        .attr('x1', deliveryIdX + rectWidth / 2)
                        .attr('y1', deliveryIdY + rectHeight)
                        .attr('x2', capabilityIdX + rectWidth / 2)
                        .attr('y2', bindingY)
                        .attr('stroke', '#333')
                        .attr('stroke-width', 2);

                    svg.append('text')
                        .attr('x', capabilityIdX + rectWidth / 2)
                        .attr('y', bindingY - 10)
                        .attr('text-anchor', 'middle')
                        .attr('fill', '#555')
                        .attr('font-size', 12)
                        .text('Binding');

                    svg.append('rect')
                        .attr('x', capabilityIdX)
                        .attr('y', bindingY)
                        .attr('width', rectWidth)
                        .attr('height', rectHeight)
                        .attr('fill', '#f9c74f');

                    svg.append('text')
                        .attr('x', capabilityIdX + rectWidth / 2)
                        .attr('y', bindingY + 30)
                        .attr('text-anchor', 'middle')
                        .attr('fill', '#000')
                        .attr('font-size', 14)
                        .text(trimId(capability.binding.bindingKey));

                    addCopyTarget(`bindingKey-${serviceProviderName}-${matchIndex}-${i}`, capability.binding.bindingKey, capabilityIdX + rectWidth / 2, bindingY + rectHeight / 2);

                    svg.append('line')
                        .attr('x1', capabilityIdX + rectWidth / 2)
                        .attr('y1', bindingY + rectHeight)
                        .attr('x2', capabilityIdX + rectWidth / 2)
                        .attr('y2', capabilityIdY)
                        .attr('stroke', '#333')
                        .attr('stroke-width', 2);

                    svg.append('text')
                        .attr('x', capabilityIdX + rectWidth / 2)
                        .attr('y', capabilityIdY - 10)
                        .attr('text-anchor', 'middle')
                        .attr('fill', '#555')
                        .attr('font-size', 12)
                        .text('CapabilityId');

                    svg.append('rect')
                        .attr('x', capabilityIdX)
                        .attr('y', capabilityIdY)
                        .attr('width', rectWidth)
                        .attr('height', rectHeight)
                        .attr('fill', '#8c8');

                    svg.append('text')
                        .attr('x', capabilityIdX + rectWidth / 2)
                        .attr('y', capabilityIdY + 30)
                        .attr('text-anchor', 'middle')
                        .attr('fill', '#000')
                        .attr('font-size', 14)
                        .text(trimId(capability.capabilityId));

                    addCopyTarget(`capabilityId-${serviceProviderName}-${matchIndex}-${i}`, capability.capabilityId, capabilityIdX + rectWidth / 2, capabilityIdY + rectHeight / 2);
                });

                yOffset += 350;
            });
        });

        setCopyTargets(targets);

    }, [matchingCapabilities]);


    return (
        <>
            {(matchingCapabilities === undefined || matchingCapabilities === null) ? (
                <Loading text="Matchin capabilities"/>
            ) : <>
                {matchingCapabilities.map((sp, index) => (
                    <Card key={index} variant="outlined" sx={{ marginBottom: 2 }}>
                        <CardContent
                            sx={{
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <Box display="flex">
                                <Typography variant="h6">{sp.serviceProviderName}</Typography>
                            </Box>
                            <IconButton onClick={handleExpandClick} size="small">
                                <Box sx={expandMoreStyle}>
                                    <ExpandMoreIcon
                                        fontSize="small"
                                        sx={{
                                            transform: expanded ? "rotate(180deg)" : "rotate(0deg)",
                                            transition: "transform 0.3s",
                                        }}
                                    />
                                </Box>
                            </IconButton>
                        </CardContent>
                        <Collapse in={expanded} timeout="auto" unmountOnExit>
                            <List>
                                <Box sx={serviceProviderStyle} />
                                <GraphSection
                                    serviceProviderName={sp.serviceProviderName}
                                    matches={sp.matches}
                                />
                            </List>
                        </Collapse>
                    </Card>
                ))}

            </>}

        </>

    );
};


const expandMoreStyle = {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    width: 35,
    height: 35,
    borderRadius: "50%",
    border: "1px solid gray",
    transition: "#gray 0.3s, border 0.3s",
    position: "relative",
    right: "-8px"
};

const serviceProviderStyle = {
    height: "1.5px",
    flexGrow: 1,
    backgroundColor: "#E67600",
    marginX: 2,
    position: "relative",
    top: "-15px"
};

export default BindingExists;
