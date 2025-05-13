import {useEffect, useRef, useState} from "react";
import {Box} from "@mui/system";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import * as d3 from 'd3';

export const GraphSection = ({ serviceProviderName, matches }: {
    serviceProviderName: string;
    matches: any[];
}) => {
    const svgRef = useRef<SVGSVGElement | null>(null);
    const [copyTargets, setCopyTargets] = useState<
        { id: string; fullId: string; x: number; y: number }[]
    >([]);

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll('*').remove();

        const width = 1000;
        const rectWidth = 120;
        const rectHeight = 50;
        const verticalSpacing = 150;
        const horizontalSpacing = 200;
        let yOffset = 50;

        const trimId = (id: string) => {
            const parts = id.split("-");
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: { id: string; fullId: string; x: number; y: number }[] = [];

        const addCopyTarget = (id: string, fullId: string, x: number, y: number) => {
            targets.push({ id, fullId, x, y });
        };

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
