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
        svg.selectAll('*').remove(); // clear previous render

        const width = 1000;

        const deliveryIdX = width / 2 - 60;
        const deliveryIdY = 50;

        svg.append('rect')
            .attr('x', deliveryIdX)
            .attr('y', deliveryIdY)
            .attr('width', 120)
            .attr('height', 50)
            .attr('fill', '#88c');

        svg.append('text')
            .attr('x', deliveryIdX + 60)
            .attr('y', deliveryIdY + 30)
            .attr('text-anchor', 'middle')
            .attr('fill', '#000')
            .text(data.deliveryId);

        const isSingle = data.capabilityMatchApi.length === 1;

        const capabilityIdY = 300;
        const totalWidth = 200 * (data.capabilityMatchApi.length - 1);
        const startX = isSingle ? width / 2 - 60 : width / 2 - totalWidth / 2;

        data.capabilityMatchApi.forEach((entry, i) => {
            const rectWidth = 120;
            const rectHeight = 50;

            const capabilityIdX = isSingle
                ? startX
                : startX + i * 200;

            // Draw capabilityIdId rectangle
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
                .text(entry.capabilityId);

            if (entry.exists) {
                svg.append('line')
                    .attr('x1', deliveryIdX + rectWidth / 2)
                    .attr('y1', deliveryIdY + rectHeight)
                    .attr('x2', capabilityIdX + rectWidth / 2)
                    .attr('y2', capabilityIdY)
                    .attr('stroke', '#000')
                    .attr('stroke-width', 2);
            }
        });
    }, []);

    return (
        <svg ref={svgRef} width={1000} height={500} />
    );
};

export default BindingExists;
