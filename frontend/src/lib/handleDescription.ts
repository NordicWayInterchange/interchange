import React from "react";

export const handleDescription = (
  event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>,
  setDescription: React.Dispatch<React.SetStateAction<string>>,
  setDescriptionError: React.Dispatch<React.SetStateAction<boolean>>
) => {
  const value = event.target.value;
  setDescription(value);
  setDescriptionError(value.length > 255);
};
