import React, { useState } from "react";
import { Card, IconButton, InputAdornment, TextField, Tooltip } from "@mui/material";
import CertificateDialog from "@/components/certificate/CertificateDialog";
import { createPKCS10 } from "@/lib/pkcs10Generator";
import { Box } from "@mui/system";
import { styled } from "@mui/material/styles";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { ICsr } from "@/interface/ICsr";
import { ICsrForm } from "@/interface/ICsrForm";
import { createCertificate } from "@/lib/fetchers/internalFetchers";
import { useSession } from "next-auth/react";
import { CertificateSignResponse } from "@/types/napcore/certificate";
import Snackbar from "@/components/shared/feedback/Snackbar";
import Subheading from "@/components/shared/display/typography/Subheading";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";

export const CertificateForm = () => {
  const {
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<ICsrForm>({
    defaultValues: {
      countryCode: "",
      orgName: "",
    },
  });
  const [csr, setCsr] = useState<ICsr>();
  const [open, setOpen] = useState<boolean>(false);
  const [error, setError] = useState<boolean>(false);
  const [chain, setChain] = useState<CertificateSignResponse>();
  const { data: session } = useSession();

  const onSubmit: SubmitHandler<ICsrForm> = (data) => {
    createPKCS10({
      commonName: session?.user.commonName,
      country: data.countryCode.toUpperCase(),
      organization: data.orgName,
    })
      .then((csr) => {
        setCsr({ csr: csr.csr, privateKey: csr.privateKey });
        void postCsr(csr);
      })
      .catch(() => {
        setError(true);
      });
  };

  const postCsr = async (csr: ICsr) => {
    const response = await createCertificate(
      session?.user.commonName as string,
      csr.csr
    );
    if (response.ok) {
      const json = await response.json();
      setChain(json);
      setOpen(true);
    } else {
      setError(true);
    }
  };

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setError(false);
  };

  const handleClickClose = (close: boolean) => {
    setOpen(close);
  };

  return (
    <StyledCard variant={"outlined"} sx={{ boxShadow: 2 }}>
      <Subheading>Create a new certificate</Subheading>
      <form onSubmit={handleSubmit(onSubmit)}>
        <StyledBox>
          <Controller
            name="countryCode"
            control={control}
            rules={{
              required: "Country code is required",
              pattern: {
                value: /^[a-z]{2}$/i,
                message: "Invalid country code",
              },
            }}
            render={({ field }) => (
              <TextField
                fullWidth
                {...field}
                label="Country code *"
                error={Boolean(errors.countryCode)}
                helperText={
                  errors.countryCode ? errors.countryCode.message : ""
                }
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <Tooltip
                          title="Country code contains of two uppercase letters."
                          placement="top"
                          slotProps={{
                            tooltip: {
                              sx: tooltipFontStyle,
                            },
                          }}
                        >
                          <IconButton edge="end" size="small">
                            <InfoOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </InputAdornment>
                    )
                  },
                }}
              />
            )}
          />
          <Controller
            name="orgName"
            control={control}
            rules={{ required: true }}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Organisation name *"
                error={Boolean(errors.orgName)}
                helperText={
                  Boolean(errors.orgName) && "Organisation name is required."
                }
              />
            )}
          />
          <StyledButton
            fullWidth
            color="buttonThemeColor"
            variant="contained"
            type="submit"
          >
            Generate certificate
          </StyledButton>
        </StyledBox>
      </form>
      <CertificateDialog
        privateKey={csr?.privateKey as string}
        chain={chain as CertificateSignResponse}
        handleDialog={handleClickClose}
        open={open}
      />
      <Snackbar
        message={"An error occured, try again!"}
        severity={"error"}
        open={error}
        handleClose={handleSnackClose}
      />
    </StyledCard>
  );
};

const StyledBox = styled(Box)(({}) => ({
  display: "flex",
  flexDirection: "column",
  gap: "20px",
  alignItems: "center",
}));

const StyledCard = styled(Card)(({}) => ({
  padding: "16px",
  display: "flex",
  width: "500px",
  gap: "24px",
  flexDirection: "column",
}));
