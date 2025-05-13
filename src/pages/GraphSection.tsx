import {useEffect, useRef, useState} from "react";
import {Box} from "@mui/system";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import * as d3 from 'd3';
import {CopyTarget, GraphSectionProps} from "@/types/GraphSection";


const GraphSection: React.FC<GraphSectionProps> = ({ serviceProviderName, matches }) => {

    const svgRef = useRef<SVGSVGElement | null>(null);
    const [copyTargets, setCopyTargets] = useState<CopyTarget[]>([]);

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll('*').remove(); // Clear previous drawing

        const width = 1000;
        const rectWidth = 120;
        const rectHeight = 50;
        const verticalSpacing = 150;
        const horizontalSpacing = 200;
        let yOffset = 50;

        const trimId = (id: string) => {
            const parts = id.split('-');
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: CopyTarget[] = [];

        const addCopyTarget = (id: string, fullId: string, x: number, y: number) => {
            targets.push({ id, fullId, x, y });
        };

        matches.forEach((match, matchIndex) => {
            const deliveryIdX = width / 2 - rectWidth / 2;
            const deliveryIdY = yOffset;

            // Draw Delivery ID
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

            addCopyTarget(`deliveryId-${matchIndex}`, match.deliveryId, deliveryIdX + rectWidth / 2, deliveryIdY + rectHeight / 2);

            // Filter capabilities that exist
            const existingCapabilities = match.capabilityMatchApi.filter((cap: { exists: any; }) => cap.exists);
            const isSingle = existingCapabilities.length === 1;
            const totalWidth = horizontalSpacing * (existingCapabilities.length - 1);
            const startX = isSingle ? width / 2 - rectWidth / 2 : width / 2 - totalWidth / 2;

            existingCapabilities.forEach((capability: { binding: { bindingKey: string; }; capabilityId: string; }, i: number) => {
                const x = startX + i * horizontalSpacing;
                const bindingY = deliveryIdY + rectHeight + 40;
                const capabilityY = bindingY + verticalSpacing;

                // Line: Delivery ➝ Binding
                svg.append('line')
                    .attr('x1', deliveryIdX + rectWidth / 2)
                    .attr('y1', deliveryIdY + rectHeight)
                    .attr('x2', x + rectWidth / 2)
                    .attr('y2', bindingY)
                    .attr('stroke', '#333')
                    .attr('stroke-width', 2);

                // Draw Binding
                svg.append('text')
                    .attr('x', x + rectWidth / 2)
                    .attr('y', bindingY - 10)
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#555')
                    .attr('font-size', 12)
                    .text('Binding');

                svg.append('rect')
                    .attr('x', x)
                    .attr('y', bindingY)
                    .attr('width', rectWidth)
                    .attr('height', rectHeight)
                    .attr('fill', '#f9c74f');

                svg.append('text')
                    .attr('x', x + rectWidth / 2)
                    .attr('y', bindingY + 30)
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#000')
                    .attr('font-size', 14)
                    .text(trimId(capability.binding.bindingKey));

                addCopyTarget(`binding-${matchIndex}-${i}`, capability.binding.bindingKey, x + rectWidth / 2, bindingY + rectHeight / 2);

                // Line: Binding ➝ Capability
                svg.append('line')
                    .attr('x1', x + rectWidth / 2)
                    .attr('y1', bindingY + rectHeight)
                    .attr('x2', x + rectWidth / 2)
                    .attr('y2', capabilityY)
                    .attr('stroke', '#333')
                    .attr('stroke-width', 2);

                // Draw Capability
                svg.append('text')
                    .attr('x', x + rectWidth / 2)
                    .attr('y', capabilityY - 10)
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#555')
                    .attr('font-size', 12)
                    .text('CapabilityId');

                svg.append('rect')
                    .attr('x', x)
                    .attr('y', capabilityY)
                    .attr('width', rectWidth)
                    .attr('height', rectHeight)
                    .attr('fill', '#8c8');

                svg.append('text')
                    .attr('x', x + rectWidth / 2)
                    .attr('y', capabilityY + 30)
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#000')
                    .attr('font-size', 14)
                    .text(trimId(capability.capabilityId));

                addCopyTarget(`capability-${matchIndex}-${i}`, capability.capabilityId, x + rectWidth / 2, capabilityY + rectHeight / 2);
            });

            yOffset += 300;
        });

        setCopyTargets(targets);
    }, [matches, serviceProviderName]);

    return (
        <div style={{ position: 'relative' }}>
            <svg ref={svgRef} width={1000} height={2000} />
            {copyTargets.map((target, index) => (
                <Box
                    key={index}
                    sx={{
                        position: 'absolute',
                        left: target.x + 15,
                        top: target.y,
                        cursor: 'pointer',
                        borderRadius: '4px',
                        paddingLeft: '10px',
                        fontSize: '16px',
                    }}
                >
                    <ContentCopy value={target.fullId} />
                </Box>
            ))}
        </div>
    );
};

export default GraphSection;