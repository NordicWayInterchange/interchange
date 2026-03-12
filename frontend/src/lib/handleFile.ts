import { fromBase64 } from "pvutils";

export const handleFile = (data: any, filename: string) => {
  const blob = new Blob([data], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.download = filename;
  link.href = url;
  link.click();
};

export const handleDecoding = (data: string | string[]) => {
  if (Array.isArray(data)) {
    return data
      .map((element) => {
        return fromBase64(element);
      })
      .join("");
  }

  return fromBase64(data);
};
