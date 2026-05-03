import IdentityLookup

final class MessageFilterExtension: ILMessageFilterExtension {}

extension MessageFilterExtension: ILMessageFilterQueryHandling {

    func handle(
        _ queryRequest: ILMessageFilterQueryRequest,
        context: ILMessageFilterExtensionContext,
        completion: @escaping (ILMessageFilterQueryResponse) -> Void
    ) {
        let response = ILMessageFilterQueryResponse()
        let (action, reason) = offlineAction(for: queryRequest)
        response.action = action

        // Record filtered messages silently — no notification, user sees it in app.
        if action == .junk {
            AppGroupStorage.shared.recordFiltered(sender: queryRequest.sender ?? "", reason: reason)
        }

        completion(response)
    }

    // MARK: - Offline decision
    private func offlineAction(for request: ILMessageFilterQueryRequest) -> (ILMessageFilterAction, String) {
        let storage = AppGroupStorage.shared

        if let sender = request.sender, storage.isTrusted(sender) {
            return (.none, "")
        }

        if let sender = request.sender, storage.isBlockedNumber(sender) {
            return (.junk, "sender")
        }

        if let body = request.messageBody, storage.containsFraudKeyword(in: body) {
            return (.junk, "keyword")
        }

        return (.none, "")
    }
}
