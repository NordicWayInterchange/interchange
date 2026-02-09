import { FieldPath, UseFormClearErrors, UseFormResetField, UseFormSetValue } from "react-hook-form";
import React from "react";

const QUADTREE_REGEX = /^[0-3]+(,[0-3]+)*$/i;
export const handleQuadtree = <TFormValues extends Record<string, any>>(
  setError: (name: FieldPath<TFormValues>, error: { type: string }) => void,
  setValue: UseFormSetValue<TFormValues>,
  clearErrors: UseFormClearErrors<TFormValues>,
  setPredefinedQuadtree: React.Dispatch<React.SetStateAction<string[]>>,
  resetField: UseFormResetField<TFormValues>
): ((event: React.ChangeEvent<HTMLInputElement>) => void) => {
  return (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    if (!QUADTREE_REGEX.test(value)) {
      setError("quadTree" as FieldPath<TFormValues>, { type: "pattern" });
      setValue("quadTree" as FieldPath<TFormValues>, value.split(",") as any);
    } else {
      setValue("quadTree" as FieldPath<TFormValues>, value.split(",") as any);
      clearErrors("quadTree" as FieldPath<TFormValues>);
    }
    setPredefinedQuadtree(value.split(","));
    if (value.length < 1) {
      resetField("quadTree" as FieldPath<TFormValues>);
    }
  };
};