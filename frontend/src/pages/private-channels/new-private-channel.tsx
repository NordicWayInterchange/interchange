import { Divider } from "@mui/material";
import React from "react";
import { Box } from "@mui/system";
import Subheading from "@/components/shared/display/typography/Subheading";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import { BreadcrumbNavigation } from "@/components/shared/actions/BreadcrumbNavigation";
import PrivateChannelsCreator from "@/components/privateChannels/PrivateChannelsCreator";
import UserAssistance from "@/components/shared/actions/UserAssistance";
const NewUserCapability = () => {

  return (
    <Box flex={1}>
      <Mainheading>Add private channel</Mainheading>
      <Subheading>
        <Box position="relative" display="inline-flex">
        Add a private channel with the form.
          <UserAssistance/>
        </Box>
      </Subheading>
      <Divider sx={{ marginY: 1 }} />
      <BreadcrumbNavigation text="Private channels" />
      <Box
        display="flex"
        flexWrap="wrap"
        gap={3}
      >
        <Box
          sx={{
            width: { xs: '100%', sm: '100%', md: '100%', lg: '50%', xl: '50%' },
          }}
        >
          <PrivateChannelsCreator />
        </Box>
      </Box>
    </Box>
  );
};

export default NewUserCapability;
