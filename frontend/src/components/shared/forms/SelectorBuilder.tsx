import { ExtendedCapability } from "@/types/capability";
import { MessageTypes } from "@/types/messageType";
import {
  Divider,
  FormControl,
  FormControlLabel,
  FormHelperText,
  IconButton,
  InputAdornment,
  InputLabel,
  MenuItem,
  Select,
  Switch,
  TextField,
  Tooltip,
} from "@mui/material";
import React, { useEffect, useState } from "react";
import { generateSelector } from "@/lib/generateSelector";
import {
  createDelivery,
  createSubscription,
} from "@/lib/fetchers/internalFetchers";
import MapDialog from "../../map/MapDialog";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { Box } from "@mui/system";
import { messageTypes } from "@/lib/data/messageTypes";
import { originatingCountries } from "@/lib/data/originatingCountries";
import { causeCodes } from "@/lib/data/causeCodes";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { IFeedback } from "@/interface/IFeedback";
import { IFormInputs } from "@/interface/IFormInputs";
import { useSession } from "next-auth/react";
import { ExtendedDelivery } from "@/types/delivery";
import { useRouter } from "next/router";
import { handleQuadtree } from "@/lib/handleQuadtree";
import {
  StyledButton,
  StyledCard,
  StyledFormControl,
  menuItemStyles,
} from "@/components/shared/styles/StyledSelectorBuilder";
import { handleDescription } from "@/lib/handleDescription";
import ConfirmSubDialog from "@/components/shared/actions/ConfirmSubDialog";
import { Cheatsheet } from "@/components/shared/forms/Cheatsheet";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";

type Props = {
  matchingElements: ExtendedCapability[] | ExtendedDelivery[] | [];
  selectorCallback: (selector: string) => void;
  label: string;
  publicationIdRow: string;
  dialogMessage: boolean;
  subscriptionConfirmationText: string;
};

const MATCHING_CAP_LIMIT = 1;
const QUADTREE_REGEX = /^[0-3]+(,[0-3]+)*$/i;

async function createArtifacts(
  artifactType: string,
  name: string,
  bodyData: Object
) {
  return await (artifactType === "Delivery"
    ? createDelivery(name, bodyData)
    : createSubscription(name, bodyData));
}

const SelectorBuilder = (props: Props) => {
  const {
    selectorCallback,
    matchingElements,
    label,
    publicationIdRow,
    dialogMessage,
    subscriptionConfirmationText,
  } = props;
  const [selector, setSelector] = useState<string>("");
  const [descriptionError, setDescriptionError] = useState(false);
  const [description, setDescription] = useState<string>("");
  const [advancedMode, setAdvancedMode] = useState<boolean>(false);
  const [persistSelector, setPersistSelector] = useState<string>("");
  const [predefinedQuadtree, setPredefinedQuadtree] = useState<string[]>([]);
  const [selectedPublicationId, setSelectedPublicationId] = useState("");
  const [open, setOpen] = useState<boolean>(false);
  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success",
  });
  const [dialogOpen, setDialogOpen] = useState<boolean>(false);
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
  const [dlqueue, seDlqueue] = useState<boolean>(false);

  const { data: session } = useSession();
  const router = useRouter();

  const handleClickClose = (close: boolean) => {
    setDialogOpen(close);
  };

  const handleMoreClose = () => {
    setDrawerOpen(true);
  };

  const {
    handleSubmit,
    control,
    getValues,
    watch,
    setValue,
    setError,
    getFieldState,
    clearErrors,
    register,
    resetField,
    reset,
    formState: { errors },
  } = useForm<IFormInputs>({
    defaultValues: {
      messageType: [],
      causeCode: [],
      protocolVersion: "",
      publisherName: "",
      publicationType: "",
      originatingCountry: [],
      publicationId: "",
      quadTree: [],
      description: ""
    },
  });

  const watchMessageType = watch("messageType");
  const DENM = MessageTypes.DENM;
  const DATEX_2 = MessageTypes.DATEX_2;

  /*
  Generate a new selector when the form state changes.
  Send the selector backwards via selectorCallback(selector) to find matching capabilities.
  */
  useEffect(() => {
    const watchAllFields = watch((value) => {
      const selector = generateSelector(value);
      selectorCallback(selector);
      setSelector(selector);
    });
    return () => watchAllFields.unsubscribe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [watch]);

  /*
  Remove cause codes from form, if the message type DENM is removed.
  */
  useEffect(() => {
    if (!watchMessageType.includes(DENM)) setValue("causeCode", []);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [watchMessageType]);

  useEffect(() => {
    reset();
    setSelectedPublicationId(publicationIdRow);
    setValue("publicationId", publicationIdRow, { shouldValidate: true });
  }, [publicationIdRow, setValue, reset]);

  const onSubmit: SubmitHandler<IFormInputs> = async () => {
    if (matchingElements.length < MATCHING_CAP_LIMIT) {
      setFeedback({
        feedback: true,
        message: "You have no matching capabilities",
        severity: "info",
      });
      return;
    }
    if (description.length > 255) return;
    setDialogOpen(true);
    if (dialogMessage) return;

    const deliveryBodyData = {
      selector: selector,
      description: description,
      dlqueue: dlqueue,
    };

    const subscriptionBodyData = {
      selector: selector,
      description: description,
    };

    const response = await createArtifacts(
      label,
      session?.user.commonName as string,
      label === "Delivery" ? deliveryBodyData : subscriptionBodyData
    );

    if (response.ok) {
      setFeedback({
        feedback: true,
        message: `${label} successfully created`,
        severity: "success",
      });
      await router.push(
        label === "Delivery" ? "/deliveries" : "/subscriptions"
      );
    } else {
      if (response.statusText === "Conflict") {
        setFeedback({
          feedback: true,
          message: `${label} already exists!`,
          severity: "warning",
        });
      } else {
        const errorData = await response.json();
        const errorMessage =
          errorData.message || `${label} could not be created, try again!`;

        setFeedback({
          feedback: true,
          message: errorMessage,
          severity: "warning",
        });
      }
    }
  };
  const handleReset = () => {
    reset();
    setSelectedPublicationId("");
    setDescription("");
    setDescriptionError(false);
    setDialogOpen(false);
  };

  const handleVerify = () => {
    selectorCallback(selector);
  };

  const handleTextArea = (event: any) => {
    const value = event.target.value;
    setSelector(value);
  };

  const handleClose = () => {
    setValue("quadTree", predefinedQuadtree);
    setOpen(false);
  };

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setFeedback({ feedback: false, message: "", severity: "success" });
  };

  const quadtreeCallback = (value: string[]) => {
    setPredefinedQuadtree(value);
  };

  /*
  Switching to advanced mode will persist the selector.
  When turning off advanced mode, the selector will be set to the persisted value.
  */
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setAdvancedMode(event.target.checked);
    if (!advancedMode) {
      setPersistSelector(selector);
    } else {
      setSelector(persistSelector);
    }
  };

  const enableDlqueue = (event: React.ChangeEvent<HTMLInputElement>) => {
    seDlqueue(event.target.checked);
  };

  return (
    <>
      <StyledCard variant={"outlined"} sx={{ boxShadow: 2 }}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <StyledFormControl>
            <Box sx={{ display: "flex", gap: 1 }}>
              <Controller
                name="publicationId"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    value={selectedPublicationId}
                    {...register("publicationId")}
                    onChange={(e) => {
                      const userInput = e.target.value;
                      field.onChange(userInput);
                      setSelectedPublicationId(userInput);
                    }}
                    disabled={advancedMode}
                    fullWidth
                    label="Publication ID"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <Tooltip
                              title="Publication ID is a unique identifier and is concatenation of publisherId with a ':' between e.g. 'DE15608:IVIM_BERLIN_067'."
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
                        ),
                      },
                    }}
                  />
                )}
              />
              <Controller
                name="originatingCountry"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth disabled={advancedMode}>
                    <InputLabel>Originating country</InputLabel>
                    <Select multiple label="Originating country" {...field}>
                      {originatingCountries.map((country, index) => (
                        <MenuItem
                          key={index}
                          value={country.value}
                          sx={menuItemStyles}
                        >
                          {country.value}
                        </MenuItem>
                      ))}
                    </Select>
                    <Box
                      position="absolute"
                      right={30}
                      top="50%"
                      sx={{
                        transform: "translateY(-50%)",
                        pointerEvents: "auto",
                      }}
                    >
                      <Tooltip
                        title="Country code (based on ISO 3166-1 alpha-2). Country code where the payload message is created"
                        arrow
                        slotProps={{
                          tooltip: {
                            sx: tooltipFontStyle,
                          },
                        }}
                      >
                        <IconButton
                          size="small"
                          sx={{
                            p: 0.5,
                          }}
                        >
                          <InfoOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </FormControl>
                )}
              />
            </Box>
            <Box sx={{ display: "flex", gap: 1 }}>
              <Controller
                name="messageType"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth disabled={advancedMode}>
                    <InputLabel>Message type</InputLabel>
                    <Select {...field} multiple label="Message type">
                      {messageTypes.map((messageType, index) => (
                        <MenuItem
                          key={index}
                          value={messageType.value}
                          sx={menuItemStyles}
                        >
                          {messageType.value}
                        </MenuItem>
                      ))}
                    </Select>
                    <Box
                      position="absolute"
                      right={30}
                      top="35%"
                      sx={{
                        transform: "translateY(-50%)",
                        pointerEvents: "auto",
                      }}
                    >
                      <Tooltip
                        title="Message type is the type of the published message"
                        arrow
                        slotProps={{
                          tooltip: {
                            sx: tooltipFontStyle,
                          },
                        }}
                      >
                        <IconButton
                          size="small"
                          sx={{
                            p: 0.5,
                          }}
                        >
                          <InfoOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </FormControl>
                )}
              />
              <Controller
                name="causeCode"
                control={control}
                render={({ field }) => (
                  <FormControl
                    fullWidth
                    disabled={!watchMessageType.includes(DENM) || advancedMode}
                  >
                    <InputLabel>Cause codes</InputLabel>
                    <Select
                      MenuProps={{ PaperProps: { sx: { maxHeight: 200 } } }}
                      multiple
                      label="Cause codes"
                      {...field}
                    >
                      {causeCodes.map((country, index) => (
                        <MenuItem
                          key={index}
                          value={country.value}
                          sx={menuItemStyles}
                        >
                          {country.value}
                          {country.label ? ":" : ""} {country.label}
                        </MenuItem>
                      ))}
                    </Select>
                    {!watchMessageType.includes(DENM) && (
                      <FormHelperText>Enable with DENM</FormHelperText>
                    )}
                  </FormControl>
                )}
              />
              {watchMessageType.includes(DATEX_2) && (
                <Controller
                  name="publisherName"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      disabled={advancedMode}
                      label="publisher name"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <Tooltip
                                title="Publisher name is the identifier for the datex message distributer and is btained from the national identifier section of the datex document."
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
                          ),
                        },
                      }}
                    />
                  )}
                />
              )}
              {watchMessageType.includes(DATEX_2) && (
                <Controller
                  name="publicationType"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      disabled={advancedMode}
                      label="publication type"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <Tooltip
                                title="Only applies for DATEX2 publications. Publication type (only one) E.g: SituationPublication or MeasuredDataPublication
or VmsPublication"
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
                          ),
                        },
                      }}
                    />
                  )}
                />
              )}
            </Box>
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Controller
                name="quadTree"
                control={control}
                rules={{ required: false, pattern: QUADTREE_REGEX }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Quadtree"
                    fullWidth
                    onChange={handleQuadtree(
                      setError,
                      setValue,
                      clearErrors,
                      setPredefinedQuadtree,
                      resetField,
                    )}
                    disabled={advancedMode}
                    error={Boolean(errors.quadTree)}
                    sx={{ marginRight: 1 }}
                    helperText={
                      "Only comma (,) separated numbers between 0-3 is allowed"
                    }
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <Tooltip
                              title="Relevant spatial index location of the C-ITS message. If a larger area is needed, you need to chain multiple quadTree values together,
                            separated by a comma."
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
                        ),
                      },
                    }}
                  />
                )}
              />
              <StyledButton
                sx={{ mt: -1 }}
                color="buttonThemeColor"
                variant="outlined"
                disabled={!!getFieldState("quadTree").error || advancedMode}
                onClick={() => setOpen(true)}
              >
                Show map
              </StyledButton>
            </Box>
            <Box>
              <TextField
                name="description"
                multiline
                rows={4}
                value={description}
                label="Description"
                onChange={(event) =>
                  handleDescription(event, setDescription, setDescriptionError)
                }
                error={descriptionError}
                helperText={
                  descriptionError
                    ? "Description exceeds maximum length of 255 characters"
                    : ""
                }
                fullWidth
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <Tooltip
                          title="Please note that the description cannot exceed 255 characters."
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
                    ),
                  },
                }}
              />
            </Box>
            <FormControlLabel
              control={<Switch onChange={handleChange} />}
              label="Advanced mode (disables all fields except description)"
            />
            {advancedMode && (
              <>
                <Divider />{" "}
                <Box sx={{ display: "flex", alignItems: "center" }}>
                  <TextField
                    multiline
                    rows={4}
                    value={selector}
                    fullWidth
                    label="Selector"
                    sx={{ marginRight: 1 }}
                    onChange={handleTextArea}
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <Tooltip
                              title="Selector can match to one or more capabilities that belong to the same Service Provider, and declares an endpoint for the a client
                            to push messages to. The system then routes messages into datastreams dependent on the capability they match."
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
                        ),
                      },
                    }}
                  />
                  <StyledButton
                    color="buttonThemeColor"
                    variant="outlined"
                    onClick={handleVerify}
                  >
                    Verify
                  </StyledButton>
                </Box>
                <Cheatsheet />
              </>
            )}
            {label === "Delivery" && (
              <FormControlLabel
                control={<Switch onChange={enableDlqueue} />}
                sx={{mt: -3 }}
                label={
                  <Box display="flex" alignItems="center">
                    <span>
                      Enable dead letter queue (DLQ) for this delivery
                    </span>
                    <Tooltip
                      slotProps={{
                        tooltip: {
                          sx: tooltipFontStyle,
                        },
                      }}
                      title="Messages that couldn't be delivered are moved to dlqueue"
                    >
                      <IconButton size="small">
                        <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                      </IconButton>
                    </Tooltip>
                  </Box>
                }
              />
            )}
            <Box sx={{ display: "flex", justifyContent: "space-evenly" }}>
              <StyledButton
                color="buttonThemeColor"
                variant="contained"
                type="submit"
                disabled={!!getFieldState("quadTree").error}
              >
                Save {label}
              </StyledButton>
              <StyledButton
                color="buttonThemeColor"
                variant="outlined"
                onClick={handleReset}
              >
                Clear form
              </StyledButton>
            </Box>
          </StyledFormControl>
        </form>
      </StyledCard>
      <MapDialog
        open={open}
        onClose={handleClose}
        quadtree={getValues("quadTree")}
        quadtreeCallback={quadtreeCallback}
      />
      {feedback.feedback && (
        <Snackbar
          message={feedback.message}
          severity={feedback.severity}
          open={feedback.feedback}
          handleClose={handleSnackClose}
        />
      )}
      {dialogMessage && (
        <ConfirmSubDialog
          open={dialogOpen}
          actorCommonName={session?.user.commonName as string}
          handleMoreClose={handleMoreClose}
          handleDialog={handleClickClose}
          selector={selector}
          description={description}
          text={subscriptionConfirmationText}
          form="SelectorBuilder"
        />
      )}
    </>
  );
};

export default SelectorBuilder;
