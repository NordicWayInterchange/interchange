import { useEffect, useState } from "react";
import { Divider } from "@mui/material";
import React from "react";
import { useMatchingCapabilities } from "@/hooks/useMatchingCapabilities";
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

const NewSubscription = () => {
  const { data: session } = useSession();
  const [selector, setSelector] = useState<string>(" ");
  const [publicationIdRow, setPublicationIdRow] = useState<string>("");
  const queryClient = useQueryClient();

  const { data, isLoading } = useMatchingCapabilities(
    session?.user.commonName as string,
    selector
  );
  const [dialogMessage, setDialogMessage] = useState<boolean>(false);
  const [subscriptionConfirmationText, setSubscriptionConfirmationText] = useState<string>("");

  useEffect(() => {
    let fullText = "";
    const total: number | undefined = data
      ?.flat()
      .reduce((sum, item) => sum + (item.shardCount ?? 0), 0);

    const isGreaterThanFive = (total ?? 0) > 5;
    if (isGreaterThanFive) {
      setDialogMessage(true);
      fullText = `Please note the total number of shards for the matching capabilities are more than five. Do you still want to subscribe?`;
      setSubscriptionConfirmationText(fullText);
    } else {
      setDialogMessage(false);
    }
  }, [data]);

  const handleChange = (selector: string) => {
    setSelector(selector);
    queryClient.removeQueries({ queryKey: ['matchingCapabilities'] });
  };

  const handleOnRowClick: GridEventListener<"rowClick"> = (params) => {
    setPublicationIdRow(params.row.publicationId);
  };

  return (
    <Box flex={1}>
      <Mainheading>Create subscription</Mainheading>
      <Subheading>
        <Box position="relative" display="inline-flex">
        Create a subscription with the form, or specify your own selector in
        advanced mode.
          <UserAssistance/>
        </Box>
      </Subheading>
      <Divider sx={{ marginY: 1 }} />
      <BreadcrumbNavigation text="Subscriptions" />
      <Box
        display="flex"
        flexWrap="wrap"
        gap={3}
      >
        <Box flex={1}
             sx={{
               width: { xs: "100%", sm: "100%", md: "100%", lg: "50%", xl: "50%" }
             }}
        >
          <SelectorBuilder
            matchingElements={data || []}
            selectorCallback={handleChange}
            publicationIdRow={publicationIdRow}
            dialogMessage={dialogMessage}
            subscriptionConfirmationText={subscriptionConfirmationText}
            label="Subscription"
          />
        </Box>
        <Box flex={1}
             sx={{
               width: { xs: "100%", sm: "100%", md: "100%", lg: "50%", xl: "50%" }
             }}
        >
          <DataGrid
            columns={NewFormDataGrid}
            rows={data || []}
            onRowClick={handleOnRowClick}
            loading={isLoading}
            getRowId={(row) => row.publicationId}
            sort={{ field: "lastStatusChange", sort: "desc" }}
            slots={{
              noRowsOverlay: CustomEmptyOverlayMatching
            }}
          />
        </Box>
      </Box>
    </Box>
  );
};

export default NewSubscription;
