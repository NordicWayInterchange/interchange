import React, { useEffect, useRef, useState } from "react";
import * as d3 from "d3";
import { Box } from "@mui/system";
import { useSession } from "next-auth/react";
import { useFetchMatchingCapabilities } from "@/hooks/useFetchMatchingCapabilities";
import {Card, CardContent, Collapse, Divider, IconButton, List, Typography} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Loading from "@/components/shared/components/Loading";
import GraphSection from "@/components/graphs/GraphSection";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";

const MatchingCapabilitiesGraph: React.FC = () => {
    const { data: session } = useSession();
    const { data: matchingCapabilities } = useFetchMatchingCapabilities(
        session?.user.commonName as string
    );

    const [expandedMap, setExpandedMap] = useState<{ [index: number]: boolean }>({});
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [selectedCapabilityId, setSelectedCapabilityId] = useState<string | null>(null);
    const [selectedDeliveryId, setSelectedDeliveryId] = useState<string | null>(null);

    const handleExpandClick = (index: number) => {
        setExpandedMap(prev => ({
            ...prev,
            [index]: !prev[index],
        }));
    };

    const handleCapabilityClick = (capabilityId: string, deliveryId: string) => {
        setSelectedCapabilityId(capabilityId);
        setSelectedDeliveryId(deliveryId);
        setDrawerOpen(true);
    };

    const handleMoreClose = () => {
        setDrawerOpen(false);
    };

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

        const trimId = (id: string | undefined | null): string => {
            if (!id || typeof id !== "string") return "";

            const parts = id.split("-");
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: { id: string; fullId: string; x: number; y: number }[] = [];

        const addCopyTarget = (id: string, fullId: string, x: number, y: number) => {
            targets.push({ id, fullId, x, y });
        };

        let yOffset = 50;

        matchingCapabilities?.forEach((entry) => {
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
        <Box flex={1}>
            <Mainheading>Graphs</Mainheading>
            <Subheading>
                These include only the service providers that have matching capabilities for each delivery. You can click on each service providers and the boxes inside
                to see details.
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            {(matchingCapabilities === undefined || matchingCapabilities === null) ? (
                <Loading text="Matching capabilities graph" />
            ) : <>
                {matchingCapabilities.some(item => item.matches?.length > 0) ? (matchingCapabilities.map((item, index) => (
                   item.matches?.length > 0 && (<Card key={index} variant="outlined" sx={{ marginBottom: 2, backgroundColor: '#F5F5F5' }}>
                        <CardContent
                            onClick={() => handleExpandClick(index)}
                            sx={{
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "space-between",
                                cursor: 'pointer'
                            }}
                        >
                            <Typography variant="h6">{item.serviceProviderName}</Typography>
                            <IconButton onClick={(e) => {
                                e.stopPropagation();
                                handleExpandClick(index);
                            }} size="small">
                                <Box sx={expandMoreStyle}>
                                    <ExpandMoreIcon
                                        fontSize="small"
                                        sx={{
                                            transform: expandedMap[index] ? "rotate(180deg)" : "rotate(0deg)",
                                            transition: "transform 0.3s",
                                        }}
                                    />
                                </Box>
                            </IconButton>
                        </CardContent>

                        <Collapse in={expandedMap[index]} timeout="auto" unmountOnExit>
                            <List>
                                <Box sx={serviceProviderStyle} />
                                <Box
                                    sx={{
                                        display: 'flex',
                                        flexWrap: 'wrap',
                                        gap: 4,
                                        justifyContent: 'flex-start',
                                    }}
                                >
                                    {item.matches
                                        .filter(match => match.capabilityMatchApi.length > 0)
                                        .map((match, matchIndex) => (
                                            <Box
                                                key={matchIndex}
                                                sx={{
                                                    flexGrow: 1,
                                                    flexBasis: {
                                                        xs: '100%',
                                                        sm: '48%',
                                                    },
                                                    minWidth: 300,
                                                }}
                                            >
                                                <GraphSection
                                                    serviceProviderName={item.serviceProviderName}
                                                    matches={[match]}
                                                />
                                            </Box>
                                        ))}
                                </Box>
                            </List>
                        </Collapse>
                    </Card>)
                ))) : (<Typography variant="body1">There is no matching capability found in any service providers</Typography>)}
            </>}

        </Box>
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
    top: 4,
};

export default MatchingCapabilitiesGraph;
