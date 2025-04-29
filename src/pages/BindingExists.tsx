import React, { useEffect, useRef } from 'react';
import * as d3 from 'd3';

const BindingExists:  React.FC = () => {

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

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll('*').remove(); // Clear previous render


        const width = 1000;
        const rectWidth = 120;
        const rectHeight = 50;

        const deliveryIdX = width / 2 - rectWidth / 2;
        const deliveryIdY = 50;

        const trimId = (id: string) => {
            const parts = id.split("-");
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        // Delivery section
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
            .text(trimId(data.deliveryId));

        const isSingle = data.capabilityMatchApi.length === 1;
        const capabilityIdY = 400;
        const bindingY = (deliveryIdY + rectHeight + capabilityIdY) / 2;
        const spacing = 200;
        const totalWidth = spacing * (data.capabilityMatchApi.length - 1);
        const startX = isSingle ? width / 2 - rectWidth / 2 : width / 2 - totalWidth / 2;

        data.capabilityMatchApi.forEach((entry, i) => {
            const capabilityIdX = isSingle
                ? startX
                : startX + i * spacing;

            //Binding section
            if (entry.exists) {
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
                    .text(trimId(entry.binding.bindingKey));

                // 3. Line: binding.key to capabilityId
                svg.append('line')
                    .attr('x1', capabilityIdX + rectWidth / 2)
                    .attr('y1', bindingY + rectHeight)
                    .attr('x2', capabilityIdX + rectWidth / 2)
                    .attr('y2', capabilityIdY)
                    .attr('stroke', '#333')
                    .attr('stroke-width', 2);
            }

            //Capability section
            svg.append('text')
                .attr('x', capabilityIdX + rectWidth / 2)
                .attr('y', capabilityIdY - 10)
                .attr('text-anchor', 'middle')
                .attr('fill', '#555')
                .attr('font-size', 12)
                .text('capabilityId');

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
                .text(trimId(entry.capabilityId));
        });
    }, []);
    
    return (
        <svg ref={svgRef} width={1000} height={500} />
    );
};

export default BindingExists;
