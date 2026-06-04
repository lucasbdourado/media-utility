export type OperationStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";

export type OperationType = "CONVERSION" | "URL_DOWNLOAD";

export type PublicErrorDetail = {
  field?: string;
  message?: string;
};

export type PublicErrorResponse = {
  code?: string;
  message?: string;
  details?: PublicErrorDetail[];
};

export type PublicResultMetadata = {
  fileName?: string;
  contentType?: string;
  sizeBytes?: number;
  downloadUrl: string;
};

export type OperationLinks = {
  status: string;
};

export type PublicOperationResponse = {
  operationId: number;
  type: OperationType;
  status: OperationStatus;
  createdAt?: string;
  completedAt?: string | null;
  expiresAt?: string | null;
  result?: PublicResultMetadata | null;
  error?: PublicErrorResponse | null;
  links: OperationLinks;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export class OperationApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly response?: PublicErrorResponse,
  ) {
    super(message);
    this.name = "OperationApiError";
  }
}

function errorMessageFromBody(error: PublicErrorResponse | undefined) {
  const detailMessages = error?.details
    ?.map((detail) => detail.message)
    .filter((message): message is string => Boolean(message?.trim()));

  if (detailMessages?.length) {
    return detailMessages.join(" ");
  }

  return error?.message?.trim() || "The request could not be completed.";
}

async function parsePublicError(response: Response) {
  try {
    return (await response.json()) as PublicErrorResponse;
  } catch {
    return undefined;
  }
}

async function readOperationResponse(response: Response) {
  if (!response.ok) {
    const errorBody = await parsePublicError(response);
    throw new OperationApiError(errorMessageFromBody(errorBody), response.status, errorBody);
  }

  return (await response.json()) as PublicOperationResponse;
}

function apiUrl(path: string) {
  return new URL(path, API_BASE_URL).toString();
}

export async function createConversion(file: File) {
  const formData = new FormData();
  formData.append("file", file);

  return readOperationResponse(
    await fetch(apiUrl("/api/operations/conversions"), {
      method: "POST",
      body: formData,
    }),
  );
}

export async function createDownload(url: string) {
  return readOperationResponse(
    await fetch(apiUrl("/api/operations/downloads"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ url }),
    }),
  );
}

export async function getOperation(statusUrl: string) {
  return readOperationResponse(await fetch(new URL(statusUrl, API_BASE_URL).toString()));
}

export function getPublicErrorMessage(error: unknown) {
  if (error instanceof OperationApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }

  return "The request could not be completed.";
}
