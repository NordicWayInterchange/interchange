import { useEffect, useRef, useState } from "react";
import { Box } from "@mui/system";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import * as d3 from "d3";
import { CopyTarget, GraphSectionProps } from "@/types/GraphSection";

const GraphSection: React.FC<GraphSectionProps> = ({ serviceProviderName, matches }) => {
    const svgRef = useRef<SVGSVGElement | null>(null);
    const [copyTargets, setCopyTargets] = useState<CopyTarget[]>([]);
    const [height, setHeight] = useState<number>(600);

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll("*").remove(); // Clear previous drawing

        const svgEl = svgRef.current;
        if (!svgEl) return;

        const baseHeight = 600;

        const rectWidth = 120;
        const rectHeight = 50;
        const verticalSpacing = 150;
        const horizontalSpacing = 300;
        let xOffset = 50;

        let maxY = 0;

        const trimId = (id: string) => {
            const parts = id.split("-");
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: CopyTarget[] = [];

        matches.forEach((match, matchIndex) => {
            const existingCapabilities = match.capabilityMatchApi?.filter((cap: { exists: any }) => cap.exists) || [];
            if (existingCapabilities.length === 0) return;

            const deliveryIdX = xOffset;
            const deliveryIdY = baseHeight / 2 - rectHeight / 2;

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
                .attr("fill", "#ffbf7d");

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
            const startY = baseHeight / 2 - rectHeight / 2;

            existingCapabilities.forEach((capability: { binding: { bindingKey: string; }; capabilityId: string; }, i: number) => {
                const bindingX = deliveryIdX + rectWidth + 40;
                const capabilityX = bindingX + rectWidth + 40;

                const offset = isSingle ? 0 : (i - (existingCapabilities.length - 1) / 2) * verticalSpacing;
                const bindingY = startY + offset;
                const capabilityY = bindingY; // same Y for now

                // Line: Delivery ➝ Binding
                if (isSingle) {
                    svg.append("line")
                        .attr("x1", deliveryIdX + rectWidth)
                        .attr("y1", deliveryIdY + rectHeight / 2)
                        .attr("x2", bindingX)
                        .attr("y2", deliveryIdY + rectHeight / 2)
                        .attr("stroke", "#333")
                        .attr("stroke-width", 2);
                } else {
                    svg.append("line")
                        .attr("x1", deliveryIdX + rectWidth)
                        .attr("y1", deliveryIdY + rectHeight / 2)
                        .attr("x2", bindingX)
                        .attr("y2", bindingY + rectHeight / 2)
                        .attr("stroke", "#333")
                        .attr("stroke-width", 2);
                }

                // Binding box
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
                    .attr("fill", "#FFF5C8");

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

                // Line: Binding ➝ Capability
                svg.append("line")
                    .attr("x1", bindingX + rectWidth)
                    .attr("y1", bindingY + rectHeight / 2)
                    .attr("x2", capabilityX)
                    .attr("y2", bindingY + rectHeight / 2)
                    .attr("stroke", "#333")
                    .attr("stroke-width", 2);

                // Capability box
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
                    .attr("fill", "#E8F3E9");

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

                // Track maximum vertical space used
                maxY = Math.max(maxY, bindingY + rectHeight);
                maxY = Math.max(maxY, capabilityY + rectHeight);
            });

            xOffset += horizontalSpacing + 300;
        });

        setHeight(Math.max(baseHeight, maxY + 100));
        setCopyTargets(targets);
    }, [matches, serviceProviderName]);

    return (
        <div style={{ position: "relative", overflowY: "auto" }}>
            <svg
                ref={svgRef}
                width="100%"
                height={height}
                style={{
                    minHeight: 600,
                    border: "1px solid red",
                }}
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
                    onClick={(e) => e.stopPropagation()}
                >
                    <ContentCopy value={target.fullId} />
                </Box>
            ))}
        </div>
    );
};

export default GraphSection;
