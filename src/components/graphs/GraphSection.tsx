import React, { useEffect, useRef, useState } from "react";
import { Box } from "@mui/system";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import * as d3 from "d3";
import { CopyTarget, GraphSectionProps } from "@/types/GraphSection";
import CapabilityDrawer from "@/components/shared/drawer/CapabilityDrawer";
import { useSession } from "next-auth/react";
import { useFetchCapabilityDetails } from "@/hooks/useFetchCapabilityDetails";

const GraphSection: React.FC<GraphSectionProps> = ({ serviceProviderName, matches }) => {
    const svgRef = useRef<SVGSVGElement | null>(null);
    const [copyTargets, setCopyTargets] = useState<CopyTarget[]>([]);
    const [graphWidth, setGraphWidth] = useState<number>(1000);
    const [graphHeight, setGraphHeight] = useState<number>(600);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [selectedCapabilityId, setSelectedCapabilityId] = useState<string | null>(null);
    const [selectedDeliveryId, setSelectedDeliveryId] = useState<string | null>(null);

    const { data: session } = useSession();

    const handleMoreClose = () => {
        setDrawerOpen(false);
    };


    const { data: capabilityDetails } = useFetchCapabilityDetails(
        session?.user.commonName as string,
        serviceProviderName,
        selectedDeliveryId,
        selectedCapabilityId
    );

    console.log(capabilityDetails)

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll("*").remove();

        const svgEl = svgRef.current;
        if (!svgEl) return;

        const topMargin = 100;
        const rectWidth = 120;
        const rectHeight = 50;
        const verticalSpacing = 60;
        const horizontalSpacing = 250;

        let xOffset = 50;
        let maxY = 0;
        let maxX = 0;

        const trimId = (id: string) => {
            const parts = id.split("-");
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: CopyTarget[] = [];

        matches.forEach((match, matchIndex) => {
            const existingCapabilities = match.capabilityMatchApi || [];
            if (existingCapabilities.length === 0) return;
            const allExist = existingCapabilities.every((cap: { exists: boolean; }) => cap.exists === true);

            const deliveryIdX = xOffset;
            const deliveryIdY = topMargin + 300;

            svg.append("text")
                .attr("x", deliveryIdX + rectWidth / 2)
                .attr("y", deliveryIdY - 10)
                .attr("text-anchor", "middle")
                .attr("fill", "#555")
                .attr("font-size", 12)
                .text("DeliveryId");

            svg.append("rect")
                .attr("x", deliveryIdX)
                .attr("y", deliveryIdY)
                .attr("width", rectWidth)
                .attr("height", rectHeight)
                .attr("fill", allExist ? "#E8F3E9" : "#B63434");

            svg.append("text")
                .attr("x", deliveryIdX + rectWidth / 2)
                .attr("y", deliveryIdY + 30)
                .attr("text-anchor", "middle")
                .attr("fill", "#000")
                .attr("font-size", 14)
                .text(trimId(match.deliveryId));

            targets.push({
                id: `deliveryId-${matchIndex}`,
                fullId: match.deliveryId,
                x: deliveryIdX + rectWidth / 2,
                y: deliveryIdY + rectHeight / 2,
            });

            const isSingle = existingCapabilities.length === 1;
            const startY = deliveryIdY;

            existingCapabilities.forEach((capability: { exists: any; binding: { bindingKey: string; }; capabilityId: string; }, i: number) => {
                const bindingX = deliveryIdX + rectWidth + 100;
                const capabilityX = bindingX + rectWidth + 40;
                const offset = isSingle ? 0 : (i - (existingCapabilities.length - 1) / 2) * verticalSpacing;
                const bindingY = startY + offset;

                svg.append("line")
                    .attr("x1", deliveryIdX + rectWidth)
                    .attr("y1", deliveryIdY + rectHeight / 2)
                    .attr("x2", bindingX)
                    .attr("y2", bindingY + rectHeight / 2)
                    .attr("stroke", "#333")
                    .attr("stroke-width", 2);

                svg.append("text")
                    .attr("x", bindingX + rectWidth / 2)
                    .attr("y", bindingY - 10)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#555")
                    .attr("font-size", 12)
                    .text("Binding");

                svg.append("rect")
                    .attr("x", bindingX)
                    .attr("y", bindingY)
                    .attr("width", rectWidth)
                    .attr("height", rectHeight)
                    .attr("fill", capability.exists ? "#FFF5C8" : "#B63434");

                svg.append("text")
                    .attr("x", bindingX + rectWidth / 2)
                    .attr("y", bindingY + 30)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#000")
                    .attr("font-size", 14)
                    .text(trimId(capability.binding.bindingKey));

                targets.push({
                    id: `binding-${matchIndex}-${i}`,
                    fullId: capability.binding.bindingKey,
                    x: bindingX + rectWidth / 2,
                    y: bindingY + rectHeight / 2,
                });

                svg.append("line")
                    .attr("x1", bindingX + rectWidth)
                    .attr("y1", bindingY + rectHeight / 2)
                    .attr("x2", capabilityX)
                    .attr("y2", bindingY + rectHeight / 2)
                    .attr("stroke", "#333")
                    .attr("stroke-width", 2);

                svg.append("text")
                    .attr("x", capabilityX + rectWidth / 2)
                    .attr("y", bindingY - 10)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#555")
                    .attr("font-size", 12)
                    .text("CapabilityId");

                svg.append("rect")
                    .attr("x", capabilityX)
                    .attr("y", bindingY)
                    .attr("width", rectWidth)
                    .attr("height", rectHeight)
                    .attr("fill", "#ffbf7d");

                svg.append("text")
                    .attr("x", capabilityX + rectWidth / 2)
                    .attr("y", bindingY + 30)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#000")
                    .attr("font-size", 14)
                    .text(trimId(capability.capabilityId));

                targets.push({
                    id: `capability-${matchIndex}-${i}`,
                    fullId: capability.capabilityId,
                    x: capabilityX + rectWidth / 2,
                    y: bindingY + rectHeight / 2,
                });

                maxY = Math.max(maxY, bindingY + rectHeight);
                maxX = Math.max(maxX, capabilityX + rectWidth);
            });

            xOffset = maxX + 150;
        });

        setCopyTargets(targets);
        setGraphWidth(Math.max(1000, maxX + 200));
        setGraphHeight(Math.max(600, maxY + topMargin));
    }, [matches, serviceProviderName]);

    return (
        <>
            <div style={{ width: "100%", overflowX: "auto" }}>
                <div
                    style={{
                        position: "relative",
                        height: 600,
                        overflowY: "auto",
                        minWidth: graphWidth,
                    }}
                >
                    <div style={{ position: "relative", height: graphHeight, width: graphWidth }}>
                        <svg
                            ref={svgRef}
                            width={graphWidth}
                            height={graphHeight}
                            style={{ display: "block" }}
                        />
                        {copyTargets.map((target, index) => (
                            <Box
                                key={index}
                                sx={{
                                    position: "absolute",
                                    left: target.x + 15,
                                    top: target.y,
                                    cursor: "pointer",
                                    borderRadius: "4px",
                                    paddingLeft: "10px",
                                    fontSize: "16px",
                                }}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    if (target.id.startsWith("capability")) {
                                        const [, matchIndexStr, capIndexStr] = target.id.split("-");
                                        const matchIndex = parseInt(matchIndexStr, 10);
                                        const capIndex = parseInt(capIndexStr, 10);

                                        const match = matches[matchIndex];
                                        const capability = match.capabilityMatchApi[capIndex];

                                        setSelectedDeliveryId(match.deliveryId);
                                        setSelectedCapabilityId(capability.capabilityId);
                                        setDrawerOpen(true);
                                    }
                                }}
                            >
                                <ContentCopy value={target.fullId} />
                            </Box>
                        ))}
                    </div>
                </div>
            </div>
            {/*<CapabilityDrawer*/}
            {/*    handleMoreClose={handleMoreClose}*/}
            {/*    open={drawerOpen}*/}
            {/*    capabilities={capabilityDetails as Capability}*/}
            {/*/>*/}
        </>
    );
};

export default GraphSection;
