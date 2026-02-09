import * as React from "react";
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
} from "@mui/material";
import { CertificateSignResponse } from "@/types/napcore/certificate";
import { useSession } from "next-auth/react";
import { handleDecoding, handleFile } from "@/lib/handleFile";
import { Box } from "@mui/system";
import { styled } from "@mui/material/styles";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

type Props = {
  privateKey: string;
  chain: CertificateSignResponse;
  open: boolean;
  handleDialog: (close: boolean) => void;
};

export default function CertificateDialog(props: Props) {
  const { privateKey, open, handleDialog, chain } = props;
  const { data: session } = useSession();

  const handleClose = () => {
    handleDialog(false);
  };

  const downloadButtons = [
    {
      text: "Download private key",
      onClick: () =>
        handleFile(privateKey, `${session?.user.commonName as string}.key.pem`),
    },
    {
      text: "Download chain certificate",
      onClick: () =>
        handleFile(
          handleDecoding(chain.chain),
          `chain.${session?.user.commonName as string}.crt.pem`
        ),
    },
    {
      text: "Download root certificate",
      onClick: () =>
        handleFile(
          handleDecoding(chain.chain.at(-1) as string),
          `root.${session?.user.commonName as string}.crt.pem`
        ),
    },
  ];

  return (
    <>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Certificate was successfully created</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ marginY: 1 }}>
            Please download your private key, chain certificate and root
            certificate before closing this window.
          </DialogContentText>
          <StyledBox>
            {downloadButtons.map((button, index) => {
              return (
                <StyledButton
                  key={index}
                  variant="text"
                  color="greenDark"
                  onClick={button.onClick}
                  disableElevation
                >
                  {button.text}
                </StyledButton>
              );
            })}
          </StyledBox>
        </DialogContent>
        <Divider />
        <DialogActions>
          <StyledButton
            variant="text"
            color="greenDark"
            onClick={handleClose}
            disableElevation
          >
            Close
          </StyledButton>
        </DialogActions>
      </Dialog>
    </>
  );
}

const StyledBox = styled(Box)(({}) => ({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
}));
