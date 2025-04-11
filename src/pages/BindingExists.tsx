import React, {useRef, useEffect, useState} from 'react';
import * as d3 from 'd3';

interface Rectangle {
    x: number;
    y: number;
    width: number;
    height: number;
    text: string;
    color: string;
}

const BindingExists: React.FC = () => {
    const svgRef = useRef<SVGSVGElement | null>(null);

    useEffect(() => {
        if (!svgRef.current) return;

        const svg = d3.select(svgRef.current).attr('width', 500).attr('height', 500);
        const rectangles: Rectangle[] = [
            { x: 50, y: 50, width: 100, height: 60, text: 'Delivery', color: '#E8F3E9' },
            { x: 350, y: 50, width: 100, height: 60, text: 'Capability', color: '#D4F7FF' },
        ];

        svg.selectAll('*').remove();

        svg
            .selectAll('rect')
            .data(rectangles)
            .enter()
            .append('rect')
            .attr('x', (d) => d.x)
            .attr('y', (d) => d.y)
            .attr('width', (d) => d.width)
            .attr('height', (d) => d.height)
            .attr('fill', (d) => d.color)
            .attr('stroke', 'black');

        svg
            .selectAll('text')
            .data(rectangles)
            .enter()
            .append('text')
            .attr('x', (d) => d.x + d.width / 2)
            .attr('y', (d) => d.y + d.height / 2)
            .attr('dy', '.35em')
            .attr('text-anchor', 'middle')
            .text((d) => d.text);


        svg
            .selectAll('line')
            .data(rectangles.slice(0, rectangles.length - 1))
            .enter()
            .append('line')
            .attr('x1', (d) => d.x + d.width)
            .attr('y1', (d) => d.y + d.height / 2)
            .attr('x2', (d, i) => rectangles[i + 1].x)
            .attr('y2', (d, i) => rectangles[i + 1].y + rectangles[i + 1].height / 2)
            .attr('stroke', 'black')
            .attr('stroke-width', 1);
    }, []);

    return <svg ref={svgRef}></svg>;
};

export default BindingExists;
