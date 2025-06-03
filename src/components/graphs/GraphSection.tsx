import React, { useEffect, useRef, useState } from 'react';
import { Box } from '@mui/system';
import * as d3 from 'd3';
import { ContentCopy } from '@/components/shared/actions/ContentCopy';
import { Capability } from '@/types/neighbours';
import { useSession } from 'next-auth/react';
import { useFetchCapabilityDetails } from '@/hooks/useFetchCapabilityDetails';
import CapabilityDrawer from '@/components/shared/drawer/CapabilityDrawer';
import BindingDrawer from '@/components/shared/drawer/BindingDrawer';
import {useFetchShardDetails} from "@/hooks/useFetchShardDetails";
import ShardDrawer from "@/components/shared/drawer/ShardDrawer";
import {Shard} from "@/types/GraphSection";

const GraphSection: React.FC<{
    serviceProviderName: string,
    matches: any[],
    handleCapabilityClick?: (capabilityId: string, deliveryId: string) => void
}> = ({serviceProviderName, matches, handleCapabilityClick}) => {
    const svgRef = useRef<SVGSVGElement | null>(null);
    const [copyTargets, setCopyTargets] = useState<{ id: string; fullId: string; x: number; y: number }[]>([]);
    const [graphWidth, setGraphWidth] = useState(1000);
    const [graphHeight, setGraphHeight] = useState(600);

    const [drawerOpen, setDrawerOpen] = useState(false);
    const [selectedCapabilityId, setSelectedCapabilityId] = useState<string | null>(null);
    const [selectedDeliveryId, setSelectedDeliveryId] = useState<string | null>(null);
    const [selectedBinding, setSelectedBinding] = useState<any | null>(null);
    const [selectedShardId, setSelectedShardId] = useState<any | null>(null);

    const { data: session } = useSession();

    const { data: capabilityDetails, refetch } = useFetchCapabilityDetails(
        session?.user.commonName as string,
        serviceProviderName,
        selectedDeliveryId,
        selectedCapabilityId
    );


    const { data: shardDetails, refetch: refetchShardDetails } = useFetchShardDetails(
        session?.user.commonName as string,
        serviceProviderName,
        selectedDeliveryId,
        selectedCapabilityId,
        selectedShardId
    );

    const handleMoreClose = () => {
        setDrawerOpen(false);
        setSelectedCapabilityId(null);
        setSelectedDeliveryId(null);
        setSelectedBinding(null);
        setSelectedShardId(null);
    };

    useEffect(() => {
        if (selectedCapabilityId && selectedDeliveryId) {
            refetch();
        }
        if (selectedCapabilityId && selectedDeliveryId && selectedShardId) {
            refetchShardDetails();
        }
    }, [selectedCapabilityId, selectedDeliveryId, refetch, refetchShardDetails, selectedShardId]);

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll('*').remove();

        const rectWidth = 120;
        const rectHeight = 50;
        const verticalSpacing = 60;
        const topMargin = 100;

        let xOffset = 50;
        let maxX = 0;
        let maxY = 0;

        const trimId = (id: string) => {
            if (typeof id !== 'string') return id;
            const parts = id.includes('-') ? id.split('-') : id;
            return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : id;
        };

        const targets: typeof copyTargets = [];

        matches.forEach((match, matchIndex) => {
            const existingCapabilities = match.capabilityMatchApi || [];
            if (existingCapabilities.length === 0) return;

            const anyExist = existingCapabilities.some((cap: { exists: boolean }) => cap.exists);

            const deliveryX = xOffset;
            const deliveryY = topMargin + 300;

            svg.append('text')
                .attr('x', deliveryX + rectWidth / 2)
                .attr('y', deliveryY - 10)
                .text('DeliveryId')
                .attr('text-anchor', 'middle')
                .attr('fill', '#555')
                .attr('font-size', 12);

            svg.append('rect')
                .attr('x', deliveryX)
                .attr('y', deliveryY)
                .attr('width', rectWidth)
                .attr('height', rectHeight)
                .attr('fill', anyExist ? '#E8F3E9' : '#B63434');

            svg.append('text')
                .attr('x', deliveryX + rectWidth / 2)
                .attr('y', deliveryY + 30)
                .text(trimId(match.deliveryId))
                .attr('text-anchor', 'middle')
                .attr('fill', '#000')
                .attr('font-size', 14);

            targets.push({
                id: `deliveryId-${matchIndex}`,
                fullId: match.deliveryId,
                x: deliveryX + rectWidth / 2,
                y: deliveryY + rectHeight / 2,
            });

            const isSingle = existingCapabilities.length === 1;
            const startY = deliveryY;

            existingCapabilities.forEach((capability: { binding: { bindingKey: string; }; capabilityId: string; shardId: string; }, i: number) => {
                const bindingX = deliveryX + rectWidth + 100;
                const capabilityX = bindingX + rectWidth + 40;
                const shardX = capabilityX + rectWidth + 40;
                const offset = isSingle ? 0 : (i - (existingCapabilities.length - 1) / 2) * verticalSpacing;
                const bindingY = startY + offset;
                svg.append('line')
                    .attr('x1', deliveryX + rectWidth)
                    .attr('y1', deliveryY + rectHeight / 2)
                    .attr('x2', bindingX)
                    .attr('y2', bindingY + rectHeight / 2)
                    .attr('stroke', '#333')
                    .attr('stroke-width', 2);

                svg.append('text')
                    .attr('x', bindingX + rectWidth / 2)
                    .attr('y', bindingY - 10)
                    .text('Binding')
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#555')
                    .attr('font-size', 12);

                svg.append('rect')
                    .attr('x', bindingX)
                    .attr('y', bindingY)
                    .attr('width', rectWidth)
                    .attr('height', rectHeight)
                    .attr('fill', '#FFF5C8')
                    .style('cursor', 'pointer')
                    .on('click', () => {
                        setSelectedBinding(capability.binding);
                        setSelectedCapabilityId(null);
                        setSelectedDeliveryId(null);
                        setDrawerOpen(true);
                    });

                svg.append('text')
                    .attr('x', bindingX + rectWidth / 2)
                    .attr('y', bindingY + 30)
                    .text(trimId(capability.binding.bindingKey))
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#000')
                    .attr('font-size', 14)
                    .style('cursor', 'pointer')
                    .on('click', () => {
                        setSelectedBinding(capability.binding);
                        setSelectedCapabilityId(null);
                        setSelectedDeliveryId(null);
                        setDrawerOpen(true);
                    });

                targets.push({
                    id: `binding-${matchIndex}-${i}`,
                    fullId: capability.binding.bindingKey,
                    x: bindingX + rectWidth / 2,
                    y: bindingY + rectHeight / 2,
                });

                // Line from binding to capability
                svg.append('line')
                    .attr('x1', bindingX + rectWidth)
                    .attr('y1', bindingY + rectHeight / 2)
                    .attr('x2', capabilityX)
                    .attr('y2', bindingY + rectHeight / 2)
                    .attr('stroke', '#333')
                    .attr('stroke-width', 2);

                // Capability box
                svg.append('text')
                    .attr('x', capabilityX + rectWidth / 2)
                    .attr('y', bindingY - 10)
                    .text('CapabilityId')
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#555')
                    .attr('font-size', 12);

                svg.append('rect')
                    .attr('x', capabilityX)
                    .attr('y', bindingY)
                    .attr('width', rectWidth)
                    .attr('height', rectHeight)
                    .attr('fill', '#ffbf7d')
                    .style('cursor', 'pointer')
                    .on('click', () => {
                        setSelectedCapabilityId(capability.capabilityId);
                        setSelectedDeliveryId(match.deliveryId);
                        setSelectedBinding(null);
                        setDrawerOpen(true);
                    });

                svg.append('text')
                    .attr('x', capabilityX + rectWidth / 2)
                    .attr('y', bindingY + 30)
                    .text(trimId(capability.capabilityId))
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#000')
                    .attr('font-size', 14)
                    .style('cursor', 'pointer')
                    .on('click', () => {
                        setSelectedCapabilityId(capability.capabilityId);
                        setSelectedDeliveryId(match.deliveryId);
                        setSelectedBinding(null);
                        setDrawerOpen(true);
                    });

                targets.push({
                    id: `capability-${matchIndex}-${i}`,
                    fullId: capability.capabilityId,
                    x: capabilityX + rectWidth / 2,
                    y: bindingY + rectHeight / 2,
                });

                // Line from capability to shard
                svg.append('line')
                    .attr('x1', capabilityX + rectWidth)
                    .attr('y1', bindingY + rectHeight / 2)
                    .attr('x2', shardX)
                    .attr('y2', bindingY + rectHeight / 2)
                    .attr('stroke', '#333')
                    .attr('stroke-width', 2);

                // Shard box
                svg.append('text')
                    .attr('x', shardX + rectWidth / 2)
                    .attr('y', bindingY - 10)
                    .text('ShardId')
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#555')
                    .attr('font-size', 12);

                svg.append('rect')
                    .attr('x', shardX)
                    .attr('y', bindingY)
                    .attr('width', rectWidth)
                    .attr('height', rectHeight)
                    .attr('fill', '#d3d3ff')
                    .style('cursor', 'pointer')
                    .on('click', () => {
                        setSelectedCapabilityId(capability.capabilityId);
                        setSelectedDeliveryId(match.deliveryId);
                        setSelectedShardId(match.shardId);
                        setSelectedBinding(null);
                        setDrawerOpen(true);
                    });

                svg.append('text')
                    .attr('x', shardX + rectWidth / 2)
                    .attr('y', bindingY + 30)
                    .text(trimId(capability.shardId as string))
                    .attr('text-anchor', 'middle')
                    .attr('fill', '#000')
                    .attr('font-size', 14)
                    .style('cursor', 'pointer')
                    .on('click', () => {
                        setSelectedCapabilityId(capability.capabilityId);
                        setSelectedDeliveryId(match.deliveryId);
                        setSelectedShardId(match.shardId);
                        setSelectedBinding(null);
                        setDrawerOpen(true);
                    });

                targets.push({
                    id: `shard-${matchIndex}-${i}`,
                    fullId: capability.shardId,
                    x: shardX + rectWidth / 2,
                    y: bindingY + rectHeight / 2,
                });

                maxX = Math.max(maxX, shardX + rectWidth);

                maxX = Math.max(maxX, capabilityX + rectWidth);
                maxY = Math.max(maxY, bindingY + rectHeight);
            });

            xOffset = maxX + 150;
        });

        setCopyTargets(targets);
        setGraphWidth(Math.max(1000, maxX + 200));
        setGraphHeight(Math.max(600, maxY + topMargin));
    }, [matches, serviceProviderName]);

    return (
        <>
            <div style={{ width: '100%', overflowX: 'auto' }}>
                <div style={{ position: 'relative', height: 600, overflowY: 'auto', minWidth: graphWidth }}>
                    <div style={{ position: 'relative', height: graphHeight, width: graphWidth }}>
                        <svg ref={svgRef} width={graphWidth} height={graphHeight} style={{ display: 'block' }} />
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
                </div>
            </div>

            {drawerOpen && selectedBinding && (
                <BindingDrawer
                    open={drawerOpen}
                    handleMoreClose={handleMoreClose}
                    binding={selectedBinding}
                />
            )}

            {drawerOpen && selectedCapabilityId && capabilityDetails && (
                <CapabilityDrawer
                    open={drawerOpen}
                    handleMoreClose={handleMoreClose}
                    capabilities={capabilityDetails as Capability}
                />
            )}

            {drawerOpen && selectedShardId && shardDetails (
                <ShardDrawer
                    open={drawerOpen}
                    handleMoreClose={handleMoreClose}
                    shard={shardDetails as Shard}
                />
            )}
        </>
    );
};

export default GraphSection;
