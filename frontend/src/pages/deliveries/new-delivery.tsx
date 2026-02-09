import { useState } from "react";
import { Divider } from "@mui/material";
import React from "react";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { useSession } from "next-auth/react";
import { Box } from "@mui/system";
import { CustomEmptyOverlayMatching } from "@/components/shared/datagrid/CustomEmptyOverlay";
import Subheading from "@/components/shared/display/typography/Subheading";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import SelectorBuilder from "@/components/shared/forms/SelectorBuilder";
import { GridEventListener } from "@mui/x-data-grid";
import { BreadcrumbNavigation } from "@/components/shared/actions/BreadcrumbNavigation";
import { NewFormDataGrid } from "@/components/shared/datagrid/GridColumns/NewFormDatagrid";
import UserAssistance from "@/components/shared/actions/UserAssistance";
import { useQueryClient } from "@tanstack/react-query";
import { useMatchingCapabilitiesForDeliveries } from "@/hooks/useMatchingCapabilitiesForDeliveries";

const NewDelivery = () => {
  const { data: session } = useSession();
  const [selector, setSelector] = useState<string>(" ");
  const [publicationIdRow, setPublicationIdRow] = useState<string>("");

  const { data, isLoading } = useMatchingCapabilitiesForDeliveries(
    session?.user.commonName as string,
    selector
  );
  const queryClient = useQueryClient();

  const handleChange = (selector: string) => {
    setSelector(selector);
    queryClient.removeQueries({ queryKey: ['matchingDeliveries'] });
  };

  const handleOnRowClick: GridEventListener<"rowClick"> = (params) => {
    setPublicationIdRow(params.row.publicationId);
  };

  return (
    <Box flex={1}>
      <Mainheading>Create Delivery</Mainheading>
      <Subheading>
        <Box position="relative" display="inline-flex">
          Create a delivery with the form, or specify your own selector in
          advanced mode.
          <UserAssistance />
        </Box>
      </Subheading>
      <Divider sx={{ marginY: 1 }} />
      <BreadcrumbNavigation text="Deliveries" />
      <Box display="flex" flexWrap="wrap" gap={3}>
        <Box
          flex={1}
          sx={{
            width: { xs: "100%", sm: "100%", md: "100%", lg: "50%", xl: "50%" },
          }}
        >
          <SelectorBuilder
            matchingElements={data || []}
            selectorCallback={handleChange}
            publicationIdRow={publicationIdRow}
            label="Delivery"
            dialogMessage={false}
            subscriptionConfirmationText=""
          />
        </Box>
        <Box
          flex={1}
          sx={{
            width: { xs: "100%", sm: "100%", md: "100%", lg: "50%", xl: "50%" },
          }}
        >
          <DataGrid
            columns={NewFormDataGrid}
            rows={data || []}
            onRowClick={handleOnRowClick}
            loading={isLoading}
            getRowId={(row) => row.publicationId}
            sort={{ field: "lastUpdatedTimestamp", sort: "desc" }}
            slots={{
              noRowsOverlay: CustomEmptyOverlayMatching,
            }}
          />
        </Box>
      </Box>
    </Box>
  );
};

export default NewDelivery;
