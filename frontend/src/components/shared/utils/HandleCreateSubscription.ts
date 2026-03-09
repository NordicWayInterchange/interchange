import { IFeedback } from "@/interface/IFeedback";
import { createSubscription } from "@/lib/fetchers/internalFetchers";
import { router } from "next/client";

export async function HandleCreateSubscription(
  name: string,
  setFeedback: (
    value: ((prevState: IFeedback) => IFeedback) | IFeedback
  ) => void,
  selector: string,
  description: string,
  form: string
) {
  const bodyData = {
    selector: selector,
    description: description,
  };
  const response = await createSubscription(name, bodyData);

  if (response.ok) {
    setFeedback({
      feedback: true,
      message: "Subscription successfully created",
      severity: "success",
    });
    if (form === "SelectorBuilder") await router.push("/subscriptions");
  } else {
    if (response.statusText === "Conflict") {
      setFeedback({
        feedback: true,
        message: "Subscription already exists!",
        severity: "warning",
      });
    } else {
      const errorData = await response.json();
      const errorMessage =
        errorData.message || "Subscription could not be created, try again!";

      setFeedback({
        feedback: true,
        message: errorMessage,
        severity: "warning",
      });
    }
  }
}
