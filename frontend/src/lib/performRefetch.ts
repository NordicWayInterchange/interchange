const logger = require("./logger");

export const performRefetch = async (refetch: any) => {
  try {
    await refetch();
  } catch (error) {
    logger.error(
      "Failed to refetch data:", error
    );
  }
};