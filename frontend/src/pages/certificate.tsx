import { Box, Divider } from "@mui/material";
import React from "react";
import { CertificateForm } from "@/components/certificate/CertificateForm";
import Mainheading from "@/components/shared/display/typography/Mainheading";

export default function Certificate() {
  return (
    <>
      <Mainheading>Certificate</Mainheading>
      <Divider sx={{ marginY: 3 }} />
      <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
        <CertificateForm />
      </Box>
    </>
  );
}
